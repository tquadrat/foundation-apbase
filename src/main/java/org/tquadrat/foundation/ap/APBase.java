/*
 * ============================================================================
 *  Copyright © 2002-2023 by Thomas Thrien.
 *  All Rights Reserved.
 * ============================================================================
 *  Licensed to the public under the agreements of the GNU Lesser General Public
 *  License, version 3.0 (the "License"). You may obtain a copy of the License at
 *
 *       http://www.gnu.org/licenses/lgpl.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

package org.tquadrat.foundation.ap;

import static java.lang.String.format;
import static java.lang.System.out;
import static java.util.stream.Collectors.toSet;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;
import static org.apiguardian.api.API.Status.STABLE;
import static org.tquadrat.foundation.lang.DebugOutput.ifDebug;
import static org.tquadrat.foundation.lang.Objects.isNull;
import static org.tquadrat.foundation.lang.Objects.requireNonNullArgument;
import static org.tquadrat.foundation.lang.Objects.requireNotEmptyArgument;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor14;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;
import org.tquadrat.foundation.annotation.ClassVersion;

/**
 *  The abstract base class for annotation processors for the Foundation
 *  Library.
 *
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @version $Id: APBase.java 1061 2023-09-25 16:32:43Z tquadrat $
 *  @since 0.1.0
 *
 *  @UMLGraph.link
 */
@SuppressWarnings( "UseOfSystemOutOrSystemErr" )
@ClassVersion( sourceVersion = "$Id: APBase.java 1061 2023-09-25 16:32:43Z tquadrat $" )
@API( status = STABLE, since = "0.1.0" )
public abstract class APBase implements Processor, APHelper
{
        /*-----------*\
    ====** Constants **========================================================
        \*-----------*/
    /**
     *  The name for sub-package that holds the generated configuration bean
     *  classes: {@value}.
     */
    public static final String PACKAGE_NAME = "generated";

    /**
     *  The name of the annotation processor option that enables the emission
     *  of some debug information into the generated code: {@value}.
     */
    public static final String ADD_DEBUG_OUTPUT = "org.tquadrat.foundation.ap.addDebugOutput";

    /**
     *  The name of the annotation processor option for the currently active
     *  Maven goal: {@value}.
     */
    public static final String MAVEN_GOAL = "org.tquadrat.foundation.ap.maven.goal";

    /**
     *  Message: {@value}.
     */
    public static final String MSG_AnnotationOnlyForFields = "%s: Only fields may be annotated with '%s'";

    /**
     *  Message: {@value}.
     */
    public static final String MSG_FieldsOnly = "Only allowed for fields";

    /**
     *  The message that indicates the illegal use of an annotation: {@value}.
     */
    public static final String MSG_IllegalAnnotationUse = "%s: Illegal use of annotation '%s'";

    /**
     *  The message that indicates that more than one element is annotated with
     *  the given annotation: {@value}.
     */
    public static final String MSG_MultipleElements = "%1$s: Multiple elements are annotated with '%2$s'";

    /**
     *  The message that indicates that more than one field is annotated with
     *  the given annotation: {@value}.
     */
    public static final String MSG_MultipleFields = "%1$s: Multiple fields are annotated with '%2$s'";

    /**
     *  The message that indicates that a String constant is required:
     *  {@value}.
     */
    public static final String MSG_StringConstantRequired = "'%s' needs to be a String constant";

        /*------------*\
    ====** Attributes **=======================================================
        \*------------*/
    /**
     *  The flag that indicates whether some debug information should be
     *  emitted to the generated code.
     */
    private boolean m_AddDebugOutput;

    /**
     *  The implementation of some utility methods for operating on elements.
     */
    private Elements m_ElementUtils;

    /**
     *  The processing environment.
     */
    private ProcessingEnvironment m_Environment;

    /**
     *  The filer that is used to create new source, class, or auxiliary files.
     */
    private Filer m_Filer;

    /**
     *  A flag that indicates whether Maven is active. This is guessed by the
     *  existence of the option <code>{@value #MAVEN_GOAL}</code>.
     */
    private boolean m_IsMavenActive = false;

    /**
     *  The
     *  {@link Messager}
     *  that is used to report errors, warnings, and other notices.
     */
    private Messager m_Messager;

    /**
     *  The options for this annotation processor.
     */
    private Map<String,String> m_Options;

    /**
     *  The implementation of some utility methods for operating on types.
     */
    private Types m_TypeUtils;

        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  Creates a new {@code APBase} instance.
     */
    protected APBase() { /* Just exists */ }

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean addDebugOutput() { return m_AddDebugOutput; }

    /**
     *  Adds the name of the annotation processor to the given message.
     *
     *  @param  msg The raw message.
     *  @return The enhanced message.
     */
    private final String composeMessage( final CharSequence msg )
    {
        final var retValue = format( "%s: %s", getClass().getSimpleName(), msg );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  composeMessage()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Iterable<? extends Completion> getCompletions( final Element element, final AnnotationMirror annotation, final ExecutableElement member, final String userText )
    {
        final List<? extends Completion> retValue = List.of();

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  getCompletions()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Elements getElementUtils() { return m_ElementUtils; }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Filer getFiler() { return m_Filer; }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Locale getLocale() { return m_Environment.getLocale(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Messager getMessager() { return this; }

    /**
     *  Retrieves the option with the given name from the annotation processors
     *  startup options.
     *
     *  @param  option  The name of the option.
     *  @return An instance of
     *      {@link Optional}
     *      that holds the option value.
     */
    protected final Optional<String> getOption( final String option )
    {
        final var retValue = Optional.ofNullable( m_Options.get( requireNotEmptyArgument( option, "option" ) ) );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  getOption()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Map<String,String> getOptions() { return Map.copyOf( m_Options ); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final SourceVersion getSourceVersion() { return m_Environment.getSourceVersion(); }

    /**
     *  Returns the classes for the supported annotations.
     *
     *  @return The supported annotations.
     */
    protected abstract Collection<Class<? extends Annotation>> getSupportedAnnotationClasses();

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Set<String> getSupportedAnnotationTypes()
    {
        final var retValue = getSupportedAnnotationClasses()
            .stream()
            .map( Class::getName )
            .collect( toSet() );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  getSupportedAnnotationTypes()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Set<String> getSupportedOptions()
    {
        final var annotation = getClass().getAnnotation( SupportedOptions.class );
        final Set<String> retValue = isNull( annotation ) ? Set.of() : Set.of( annotation.value() );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  getSupportedOptions()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final SourceVersion getSupportedSourceVersion()
    {
        final var annotation = getClass().getAnnotation( SupportedSourceVersion.class );
        final SourceVersion retValue;
        if( isNull( annotation ) )
        {
            retValue = SourceVersion.RELEASE_15;
            printMessage( WARNING, "No SupportedSourceVersion annotation found on '%s', returning '%s'.".formatted( getClass().getName(), retValue.toString() ) );
        }
        else
        {
            retValue = annotation.value();
        }

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  getSupportedSourceVersion()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Types getTypeUtils() { return m_TypeUtils; }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isEnumType( final TypeMirror type )
    {
        final var enumType = m_TypeUtils.erasure( m_ElementUtils.getTypeElement( Enum.class.getName() ).asType() );
        final var focusType = m_TypeUtils.erasure( requireNonNullArgument( type, "type" ) );
        final var retValue = m_TypeUtils.isSubtype( focusType, enumType );
        ifDebug( "type: %1$s%n\tenumType: %2$s%n\tfocusType: %3$s%n\tisEnumType: %4$b"::formatted, type, enumType, focusType, retValue );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  isEnumType()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final void init( final ProcessingEnvironment processingEnv )
    {
        //---* Keep the processing environment *-------------------------------
        m_Environment = processingEnv;
        m_Messager = m_Environment.getMessager();
        m_Filer = m_Environment.getFiler();
        m_ElementUtils = m_Environment.getElementUtils();
        m_TypeUtils = m_Environment.getTypeUtils();
        m_Options = m_Environment.getOptions();

        m_IsMavenActive = m_Options.containsKey( MAVEN_GOAL );
        m_AddDebugOutput = m_Options.containsKey( ADD_DEBUG_OUTPUT );

        printMessage( NOTE, "Annotation Processor invoked!" );
        printMessage( NOTE, "Class: %s".formatted( getClass().getName() ) );
        for( final var option : m_Options.entrySet() )
        {
            printMessage( NOTE, "Option: %s=%s".formatted( option.getKey(), option.getValue() ) );
        }
    }   //  init()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final void printMessage( final Diagnostic.Kind kind, final CharSequence msg )
    {
        final var message = composeMessage( msg );
        m_Messager.printMessage( kind, message );
        if( m_IsMavenActive )
        {
            out.printf( "[%s] %s%n", kind.name(), message );
        }
    }   //  printMessage()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final void printMessage( final Diagnostic.Kind kind, final CharSequence msg, final Element element )
    {
        final var message = composeMessage( msg );
        m_Messager.printMessage( kind, message, element );
        if( m_IsMavenActive )
        {
            out.printf( "[%s] %s (Element: %s)%n", kind.name(), message, element.getSimpleName() );
        }
    }   //  printMessage()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final void printMessage( final Diagnostic.Kind kind, final CharSequence msg, final Element element, final AnnotationMirror annotation )
    {
        final var message = composeMessage( msg );
        m_Messager.printMessage( kind, message, element, annotation );
        if( m_IsMavenActive )
        {
            out.printf( "[%s] %s (Element: %s/Annotation: %s)%n", kind.name(), message, element.getSimpleName(), annotation.getAnnotationType().asElement().getSimpleName() );
        }
    }   //  printMessage()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final void printMessage( final Diagnostic.Kind kind, final CharSequence msg, final Element element, final AnnotationMirror annotation, final AnnotationValue value )
    {
        final var message = composeMessage( msg );
        m_Messager.printMessage( kind, message, element, annotation, value );
        if( m_IsMavenActive )
        {
            out.printf( "[%s] %s (Element: %s/Annotation: %s = %s)%n", kind.name(), message, element.getSimpleName(), annotation.getAnnotationType().asElement().getSimpleName(), value.toString() );
        }
    }   //  printMessage()

    /**
     *  {@inheritDoc}
     */
    @Override
    public abstract boolean process( final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment );

    /**
     *  <p>{@summary Retrieves the field that is annotated with the given
     *  annotation.} The expectation is that there is just one
     *
     *  @param  roundEnvironment    The environment for information about the
     *      current and prior round as set to
     *      {@link #process(Set, RoundEnvironment)}.
     *  @param  annotationClass The class of the annotation.
     *  @return An instance of
     *      {@link Optional}
     *      that holds the field.
     *  @throws IllegalAnnotationError  The annotation is invalid.
     *  @throws CodeGenerationError Multiple fields are annotated with the
     *      given annotation.
     */
    protected final Optional<VariableElement> retrieveAnnotatedField( final RoundEnvironment roundEnvironment, final Class<? extends Annotation> annotationClass ) throws IllegalAnnotationError
    {
        requireNonNullArgument( annotationClass, "annotationClass" );
        ifDebug( currentClass -> "annotationClass: %s".formatted( ((Class<?>) currentClass [0]).getName() ), annotationClass );
        Optional<VariableElement> retValue = Optional.empty();
        String errorMessage = null;

        ifDebug( currentClass -> "annotationClass '%s' is in annotations".formatted( ((Class<?>) currentClass [0]).getName() ), annotationClass );
        VariableElement lastElement = null;
        var isInError = false;
        ScanLoop: for( final var element : requireNonNullArgument( roundEnvironment, "roundEnvironment" ).getElementsAnnotatedWith( annotationClass ) )
        {
            if( element instanceof final VariableElement variableElement )
            {
                if( variableElement.getKind() == ElementKind.FIELD )
                {
                    if( isNull( lastElement ) )
                    {
                        lastElement = variableElement;
                        final var value = variableElement.getConstantValue();
                        if( value instanceof String )
                        {
                            retValue = Optional.of( variableElement );
                        }
                        else
                        {
                            isInError = true;
                            errorMessage = format( MSG_StringConstantRequired, element.getSimpleName().toString() );
                            printMessage( ERROR, errorMessage, element );
                            break ScanLoop;
                        }
                    }
                    else
                    {
                        if( !isInError )
                        {
                            isInError = true;
                            errorMessage = format( MSG_MultipleFields, variableElement.getSimpleName().toString(), annotationClass.getSimpleName() );
                            printMessage( ERROR, errorMessage, lastElement );
                        }

                        //---* Print the other elements, too *-----------------
                        printMessage( ERROR, format( MSG_MultipleFields, variableElement.getSimpleName().toString(), annotationClass.getSimpleName() ), element );
                    }
                }
                else
                {
                    printMessage( ERROR, format( MSG_AnnotationOnlyForFields, variableElement.getSimpleName().toString(), annotationClass.getSimpleName() ), element );
                    throw new IllegalAnnotationError( MSG_FieldsOnly, annotationClass );
                }
            }
            else
            {
                errorMessage = format( MSG_IllegalAnnotationUse, element.getSimpleName().toString(), annotationClass.getSimpleName() );
                printMessage( ERROR, errorMessage, element );
                throw new IllegalAnnotationError( errorMessage );
            }
        }   //  ScanLoop:
        if( isInError ) throw new CodeGenerationError( errorMessage );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  retrieveAnnotatedField()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final List<Name> retrieveArgumentNames( final ExecutableElement method )
    {
        final var retValue = requireNonNullArgument( method, "method" ).getParameters()
            .stream()
            .map( VariableElement::getSimpleName )
            .toList();

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  retrieveArgumentNames()

    /**
     *  {@inheritDoc}
     */
    @SuppressWarnings( {"AnonymousInnerClassMayBeStatic", "AnonymousInnerClass"} )
    @Override
    public List<TypeMirror> retrieveGenericTypes( final TypeMirror type )
    {
        final Collection<TypeMirror> buffer = new ArrayList<>();

        //noinspection CollectionAddAllCanBeReplacedWithConstructor
        buffer.addAll( type.accept( new SimpleTypeVisitor14<Collection<TypeMirror>, Void>( List.of() )
        {
            /**
             *  {@inheritDoc}
             */
            @SuppressWarnings( "unchecked" )
            @Override
            public final Collection<TypeMirror> visitDeclared( final DeclaredType declaredType, final Void ignored )
            {
                return (Collection<TypeMirror>) declaredType.getTypeArguments();
            }
        }, null ) );

        //---* Create the return value *---------------------------------------
        final var retValue = List.copyOf( buffer );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  retrieveGenericTypes()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final void retrieveInterfaces( final TypeElement typeElement, final Set<? super TypeElement> interfaces )
    {
        interfaces.add( typeElement );

        for( final var typeMirror : typeElement.getInterfaces() )
        {
            final var nextInterface = (TypeElement) m_TypeUtils.asElement( typeMirror );
            if( !interfaces.contains( nextInterface ) )
            {
                retrieveInterfaces( nextInterface, interfaces );
            }
        }
    }   //  retrieveInterfaces()
}
//  class APBase

/*
 *  End of File
 */