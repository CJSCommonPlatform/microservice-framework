package uk.gov.justice.raml.jms.interceptor;

import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.JavaFile.builder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.raml.jms.core.ClassNameFactory.JMS_LOGGER_METADATA_INTERCEPTOR;
import static uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility.javaCompilerUtil;

import uk.gov.justice.raml.jms.core.ClassNameFactory;
import uk.gov.justice.services.adapter.messaging.JmsLoggerMetadataAdder;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.interceptor.InvocationContext;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JmsLoggerMetadataInterceptorCodeGeneratorTest {

    private static final String SERVICE_COMPONENT_NAME = "CUSTOM";

    @TempDir
    public File temporaryFolder;

    @InjectMocks
    private JmsLoggerMetadataInterceptorCodeGenerator jmsLoggerMetadataInterceptorCodeGenerator;

    @Test
    public void shouldGenerateJmsLoggerMetadataInterceptor() throws Exception {

        final String packageName = "uk.gov.justice.api.interceptor";
        final String simpleName = "MyCustomJmsLoggerMetadataInterceptorCodeGenerator";

        final ClassName jmsLoggerMetadataInterceptorClassName = get(packageName, simpleName);
        final ClassNameFactory classNameFactory = mock(ClassNameFactory.class);
        final CommonGeneratorProperties commonGeneratorProperties = mock(CommonGeneratorProperties.class);

        when(commonGeneratorProperties.getServiceComponent()).thenReturn(SERVICE_COMPONENT_NAME);
        when(classNameFactory.classNameFor(JMS_LOGGER_METADATA_INTERCEPTOR)).thenReturn(jmsLoggerMetadataInterceptorClassName);

        final TypeSpec typeSpec = jmsLoggerMetadataInterceptorCodeGenerator.generate(
                commonGeneratorProperties,
                classNameFactory);

        final File outputDirectory = new File(temporaryFolder, "test-generation");
        outputDirectory.mkdirs();
        builder(packageName, typeSpec)
                .build()
                .writeTo(outputDirectory);

        File outputDir = new File(temporaryFolder, getClass().getSimpleName());
        outputDir.mkdirs();
        final Class<?> compiledClass = javaCompilerUtil().compiledClassOf(
                outputDirectory,
                outputDir,
                packageName,
                simpleName);

        testGeneratedClass(compiledClass);
    }

    private void testGeneratedClass(final Class<?> compiledClass) throws Exception {

        final Object myCustomJmsLoggerMetadataInterceptor = compiledClass.newInstance();
        final JmsLoggerMetadataAdder jmsLoggerMetadataAdder = mock(JmsLoggerMetadataAdder.class);
        final InvocationContext invocationContext = mock(InvocationContext.class);
        final Object expected = mock(Object.class);

        final Field jmsLoggerMetadataAdderField = compiledClass.getDeclaredField("jmsLoggerMetadataAdder");
        jmsLoggerMetadataAdderField.setAccessible(true);
        jmsLoggerMetadataAdderField.set(myCustomJmsLoggerMetadataInterceptor, jmsLoggerMetadataAdder);

        when(jmsLoggerMetadataAdder.addRequestDataToMdc(invocationContext, SERVICE_COMPONENT_NAME)).thenReturn(expected);

        final Method addRequestDataToMdc = compiledClass.getMethod("addRequestDataToMdc", InvocationContext.class);

        final Object result = addRequestDataToMdc.invoke(myCustomJmsLoggerMetadataInterceptor, invocationContext);

        assertThat(result, is(expected));

        verify(jmsLoggerMetadataAdder).addRequestDataToMdc(invocationContext, SERVICE_COMPONENT_NAME);
    }
}