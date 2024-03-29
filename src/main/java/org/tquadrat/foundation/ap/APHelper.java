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

import static java.lang.reflect.Proxy.isProxyClass;
import static org.apiguardian.api.API.Status.STABLE;
import static org.tquadrat.foundation.lang.CommonConstants.EMPTY_STRING;
import static org.tquadrat.foundation.lang.DebugOutput.ifDebug;
import static org.tquadrat.foundation.lang.Objects.requireNonNullArgument;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import org.apiguardian.api.API;
import org.tquadrat.foundation.annotation.ClassVersion;

/**
 *  The specification for a set of helpers for annotation processing.
 *
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @version $Id: APHelper.java 1061 2023-09-25 16:32:43Z tquadrat $
 *  @since 0.1.0
 *
 *  @UMLGraph.link
 */
@ClassVersion( sourceVersion = "$Id: APHelper.java 1061 2023-09-25 16:32:43Z tquadrat $" )
@API( status = STABLE, since = "0.1.0" )
public interface APHelper extends Messager, ProcessingEnvironment
{
        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  <p>{@summary Returns the flag that indicates whether some debug
     *  information should be emitted to the generated code.}</p>
     *  <p>The value is controlled by the value of the annotation processor
     *  option
     *  {@value APBase#ADD_DEBUG_OUTPUT}.</p>
     *
     *  @return {@code true} if the debug information should be added,
     *      {@code false} if not.
     */
    @SuppressWarnings( "BooleanMethodNameMustStartWithQuestion" )
    public boolean addDebugOutput();

    /**
     *  Retrieves the
     *  {@link AnnotationMirror}
     *  for the given annotation
     *  from the given
     *  {@link Element}
     *  instance. Only declared annotations are considered, not the inherited
     *  ones.
     *
     *  @param  element The element.
     *  @param  annotationClass The annotation to look for.
     *  @return An instance of
     *      {@link Optional}
     *      that holds the found {@code AnnotationMirror} instance.
     *
     *  @see Element#getAnnotationMirrors()
     */
    public default Optional<? extends AnnotationMirror> getAnnotationMirror( final Element element, final Class<? extends Annotation> annotationClass )
    {
        final var isProxyClass = isProxyClass( requireNonNullArgument( annotationClass, "annotationClass" ) );
        ifDebug( a -> "annotationClass = %s".formatted( ((Class<?>) a [0]).getName() ), annotationClass );
        final String annotationClassName;
        if( isProxyClass )
        {
            final var interfaces = annotationClass.getInterfaces();
            ifDebug( interfaces.length > 1, currentInterfaces ->
            {
                final var joiner = new StringJoiner( ", ", "annotation interface: ", EMPTY_STRING );
                //noinspection SuspiciousArrayCast
                for( final var currentInterface : ((Class<?> []) currentInterfaces) )
                {
                    joiner.add( currentInterface.getName() );
                }
                return joiner.toString();
            }, (Object []) interfaces );
            if( interfaces.length != 1 )
                throw new AnnotationProcessingError( "annotationClass proxy '%s' implements more than one interface".formatted( annotationClass.getName() ) );
            annotationClassName = interfaces [0].getName();
        }
        else
        {
            annotationClassName = requireNonNullArgument( annotationClass, "annotationClass" ).getName();
        }
        ifDebug( "effective annotationClassName = %s"::formatted, annotationClassName );
        final var retValue = requireNonNullArgument( element, "element" ).getAnnotationMirrors().stream()
            .filter( annotationMirror -> annotationMirror.getAnnotationType().toString().equals( annotationClassName ) )
            .findFirst();
        ifDebug( retValue.isEmpty(), e ->
        {
            final var joiner = new StringJoiner( ", ", "annotationMirrors: ", EMPTY_STRING );
            for( final var mirror : ((Element) e [0]).getAnnotationMirrors() )
            {
                joiner.add( mirror.getAnnotationType().toString() );
            }
            return joiner.toString();
        }, element );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  getAnnotationMirror()

    /**
     *  Retrieves the annotation value from the given annotation mirror.
     *
     *  @param  annotationMirror    The annotation mirror.
     *  @return An instance of
     *      {@link Optional}
     *      that holds the found annotation value.
     */
    public default Optional<? extends AnnotationValue> getAnnotationValue( final AnnotationMirror annotationMirror )
    {
        final var retValue = getAnnotationValue( annotationMirror, "value" );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  getAnnotationValue()

    /**
     *  Retrieves the annotation value with the given name from the given
     *  annotation mirror.
     *
     *  @param  annotationMirror    The annotation mirror.
     *  @param  name    The name of the desired value.
     *  @return An instance of
     *      {@link Optional}
     *      that holds the found annotation value.
     */
    public default Optional<? extends AnnotationValue> getAnnotationValue( final AnnotationMirror annotationMirror, final String name )
    {
        final var elementValues = getElementUtils().getElementValuesWithDefaults( annotationMirror );
        @SuppressWarnings( "unlikely-arg-type" )
        final var retValue = elementValues.keySet().stream()
            .filter( executableElement -> executableElement.getSimpleName().toString().equals( name ) )
            .map( elementValues::get )
            .findAny();

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  getAnnotationValue()

    /**
     *  <p>{@summary Retrieves a
     *  {@link TypeMirror}
     *  instance from an annotation.}</p>
     *  <p>In case an annotation defines a
     *  {@link Class Class&lt;?&gt;}
     *  attribute, the value for that attribute is either {@code null} or
     *  something strange, but never an instance of {@code Class<?>}. So
     *  we need some special code to get something useful from the
     *  annotation.</p>
     *  <p>This implementations assumes the default name
     *  &quot;{@code value}&quot; for the attribute.</p>
     *
     *  @param  element The annotated element.
     *  @param  annotationClass The type of the annotation.
     *  @return An instance of
     *      {@link Optional}
     *      that holds the {@code TypeMirror}
     *      instance.
     *  @throws NoSuchElementException  The given element is not annotated with
     *      an annotation of the given type.
     */
    public default Optional<TypeMirror> getTypeMirrorFromAnnotationValue( final Element element, final Class<? extends Annotation> annotationClass )
    {
        final var annotationMirror = getAnnotationMirror( element, annotationClass )
            .orElseThrow( () -> new NoSuchElementException( annotationClass.getName() ) );

        final var retValue = getAnnotationValue( annotationMirror )
            .map( annotationValue -> (TypeMirror) annotationValue.getValue() );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  getTypeMirrorFromAnnotationValue()

    /**
     *  <p>{@summary Retrieves a
     *  {@link TypeMirror}
     *  instance from an annotation.}</p>
     *  <p>In case an annotation defines a
     *  {@link Class Class&lt;?&gt;}
     *  attribute, the value for that attribute is either {@code null} or
     *  something strange, but never an instance of {@code Class<?>}. So we
     *  need some special code to get something useful from the annotation.</p>
     *
     *  @param  element The annotated element.
     *  @param  annotationClass The type of the annotation.
     *  @param  attributeName   The name of the attribute that holds the class.
     *  @return An instance of
     *      {@link Optional}
     *      that holds the {@code TypeMirror}
     *      instance.
     *  @throws NoSuchElementException  The given element is not annotated with
     *      an annotation of the given type.
     */
    public default Optional<TypeMirror> getTypeMirrorFromAnnotationValue( final Element element, final Class<? extends Annotation> annotationClass, final String attributeName )
    {
        final var annotationMirror = getAnnotationMirror( element, annotationClass )
            .orElseThrow( () -> new NoSuchElementException( annotationClass.getName() ) );

        final var retValue = getAnnotationValue( annotationMirror, attributeName )
            .map( annotationValue -> (TypeMirror) annotationValue.getValue() );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  getTypeMirrorFromAnnotationValue()

    /**
     *  Checks whether the given element has the given annotation.
     *
     *  @note If the retention for the given annotation is not
     *      {@link java.lang.annotation.RetentionPolicy#RUNTIME RUNTIME}
     *      it could have been removed from the element in an earlier compile
     *      run.
     *
     *  @param  <A> The type of the annotation.
     *  @param  element The element to inspect.
     *  @param  annotationType  The type of the annotation to look for.
     *  @return {@code true} if the element is annotated with the given
     *      annotation, {@code false} if not.
     *
     *  @see java.lang.annotation.RetentionPolicy#RUNTIME
     */
    public default <A extends Annotation> boolean hasAnnotation( final Element element, final Class<A> annotationType )
    {
        final var annotation = requireNonNullArgument( element, "element" ).getAnnotation( requireNonNullArgument( annotationType, "annotationType" ) );
        final var retValue = annotation != null;

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  hasAnnotation()

    /**
     *  Determines whether the given instance of
     *  {@link TypeMirror}
     *  is for an
     *  {@link Enum}
     *  type.
     *
     *  @param  type    The type to check.
     *  @return {@code true} if the given type is an {@code Enum} type,
     *      {@code false} otherwise.
     */
    public boolean isEnumType( final TypeMirror type );

    /**
     *  {@inheritDoc}
     */
    @Override
    public default void printMessage( final Diagnostic.Kind kind, final CharSequence msg )
    {
        getMessager().printMessage( kind, msg );
    }   //  printMessage()

    /**
     *  {@inheritDoc}
     */
    @Override
    public default void printMessage( final Diagnostic.Kind kind, final CharSequence msg, final Element element )
    {
        getMessager().printMessage( kind, msg, element );
    }   //  printMessage()

    /**
     *  {@inheritDoc}
     */
    @Override
    public default void printMessage( final Diagnostic.Kind kind, final CharSequence msg, final Element element, final AnnotationMirror annotation )
    {
        getMessager().printMessage( kind, msg, element, annotation );
    }   //  printMessage()

    /**
     *  {@inheritDoc}
     */
    @Override
    public default void printMessage( final Diagnostic.Kind kind, final CharSequence msg, final Element element, final AnnotationMirror annotation, final AnnotationValue value )
    {
        getMessager().printMessage( kind, msg, element, annotation, value );
    }   //  printMessage()

    /**
     *  <p>{@summary Retrieves the names of a method's arguments.}</p>
     *  <p>This method will return the names of the arguments as defined only
     *  if the compiler flag {@code -parameters} is set; otherwise, the
     *  arguments are just counted ({@code arg0}, {@code arg1}, {@code arg2},
     *  …).</p>
     *
     *  @param  method  The method.
     *  @return The names of the arguments.
     */
    public List<Name> retrieveArgumentNames( final ExecutableElement method );

    /**
     *  <p>{@summary Retrieves the generic types for the given type.}</p>
     *
     *  @param  type    The
     *      {@link TypeMirror} instance to inspect.
     *  @return The type arguments the given type was defined with; will be the
     *      empty list if that type was not generic.
     */
    public List<? extends TypeMirror> retrieveGenericTypes( final TypeMirror type );

    /**
     *  Retrieves the interfaces are that implemented or extended by the given
     *  type element.
     *
     *  @param  typeElement The type element to inspect.
     *  @param  interfaces  The already retrieved interfaces.
     */
    public default void retrieveInterfaces( final TypeElement typeElement, final Set<? super TypeElement> interfaces )
    {
        interfaces.add( typeElement );

        for( final var typeMirror : typeElement.getInterfaces() )
        {
            final var nextInterface = (TypeElement) getTypeUtils().asElement( typeMirror );
            if( !interfaces.contains( nextInterface ) )
            {
                retrieveInterfaces( nextInterface, interfaces );
            }
        }
    }   //  retrieveInterfaces()
}
//  class APHelper

/*
 *  End of File
 */