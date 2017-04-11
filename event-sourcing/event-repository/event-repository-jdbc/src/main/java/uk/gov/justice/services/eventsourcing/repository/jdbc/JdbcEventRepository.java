package uk.gov.justice.services.eventsourcing.repository.jdbc;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidStreamIdException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

/**
 * JDBC implementation of {@link EventRepository}
 */
public class JdbcEventRepository implements EventRepository {

    @Inject
    Logger logger;

    @Inject
    EventLogConverter eventLogConverter;

    @Inject
    EventLogJdbcRepository eventLogJdbcRepository;

    @Override
    public Stream<JsonEnvelope> getByStreamId(final UUID streamId) {
        if (streamId == null) {
            throw new InvalidStreamIdException("streamId is null.");
        }

        logger.trace("Retrieving event stream for {}", streamId);
        return eventLogJdbcRepository.findByStreamIdOrderBySequenceIdAsc(streamId)
                .map(eventLogConverter::envelopeOf);
    }

    @Override
    public Stream<JsonEnvelope> getByStreamIdAndSequenceId(final UUID streamId, final Long sequenceId) {
        if (streamId == null) {
            throw new InvalidStreamIdException("streamId is null.");
        } else if (sequenceId == null) {
            throw new JdbcRepositoryException("sequenceId is null.");
        }

        logger.trace("Retrieving event stream for {} at sequence {}", streamId, sequenceId);
        return eventLogJdbcRepository.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(streamId, sequenceId)
                .map(eventLogConverter::envelopeOf);

    }

    @Override
    public Stream<JsonEnvelope> getAll() {
        logger.trace("Retrieving all events");
        return eventLogJdbcRepository.findAll()
                .map(eventLogConverter::envelopeOf);
    }

    @Override
    @Transactional(dontRollbackOn = OptimisticLockingRetryException.class)
    public void store(final JsonEnvelope envelope) throws StoreEventRequestFailedException {
        try {
            final EventLog eventLog = eventLogConverter.eventLogOf(envelope);
            logger.trace("Storing event {} into stream {} at version {}", eventLog.getName(), eventLog.getStreamId(), eventLog.getSequenceId());
            eventLogJdbcRepository.insert(eventLog);
        } catch (InvalidSequenceIdException ex) {
            throw new StoreEventRequestFailedException(String.format("Could not store event for version %d of stream %s",
                    envelope.metadata().version().orElse(null), envelope.metadata().streamId().orElse(null)), ex);
        }
    }

    @Override
    public long getCurrentSequenceIdForStream(final UUID streamId) {
        return eventLogJdbcRepository.getLatestSequenceIdForStream(streamId);
    }

    @Override
    public Stream<Stream<JsonEnvelope>> getStreamOfAllEventStreams() {
        final Stream<UUID> streamIds = eventLogJdbcRepository.getStreamIds();
        return streamIds
                .map(id -> {
                    final Stream<EventLog> eventStream = eventLogJdbcRepository.findByStreamIdOrderBySequenceIdAsc(id);
                    streamIds.onClose(eventStream::close);
                    return eventStream.map(eventLogConverter::envelopeOf);
                });

    }
}
