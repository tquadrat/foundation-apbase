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

import static org.apiguardian.api.API.Status.STABLE;
import static org.tquadrat.foundation.lang.Objects.requireNonNullArgument;

import java.io.Serial;
import java.lang.annotation.Annotation;

import org.apiguardian.api.API;
import org.tquadrat.foundation.annotation.ClassVersion;

/**
 *  Signals an incorrect use of an annotations.
 *
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @version $Id: IllegalAnnotationError.java 1061 2023-09-25 16:32:43Z tquadrat $
 *  @since 0.1.0
 *
 *  @UMLGraph.link
 */
@SuppressWarnings( "ClassWithTooManyConstructors" )
@ClassVersion( sourceVersion = "$Id: IllegalAnnotationError.java 1061 2023-09-25 16:32:43Z tquadrat $" )
@API( status = STABLE, since = "0.1.0" )
public final class IllegalAnnotationError extends AnnotationProcessingError
{
        /*------------------------*\
    ====** Static Initialisations **===========================================
        \*------------------------*/
    /**
     *  The serial version UID for objects of this class: {@value}.
     */
    @Serial
    private static final long serialVersionUID = 2397757838147693218L;

        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  Creates a new {@code IllegalAnnotationError} instance.
     *
     *  @param  message The message for the error.
     */
    public IllegalAnnotationError( final String message ) { super( requireNonNullArgument( message, "message" ) ); }

    /**
     *  Creates a new {@code IllegalAnnotationError} instance.
     *
     *  @param  <A> The type of the illegal annotation.
     *  @param  annotation  The invalid annotation.
     */
    public <A extends Annotation> IllegalAnnotationError( final A annotation )
    {
        this( requireNonNullArgument( annotation, "annotation" ).getClass() );
    }   //  IllegalAnnotationError()

    /**
     *  Creates a new {@code IllegalAnnotationError} instance.
     *
     *  @param  annotationClass The class of the invalid annotation.
     */
    public IllegalAnnotationError( final Class<? extends Annotation> annotationClass )
    {
        this( "Annotation '%s' is illegal".formatted( requireNonNullArgument( annotationClass, "annotationClass" ).getName() ) );
    }   //  IllegalAnnotationError()

    /**
     *  Creates a new {@code IllegalAnnotationError} instance.
     *
     *  @param  <A> The type of the illegal annotation.
     *  @param  message The message for the error.
     *  @param  annotation  The invalid annotation.
     */
    public <A extends Annotation> IllegalAnnotationError( final String message, final A annotation )
    {
        this( message, requireNonNullArgument( annotation, "annotation" ).getClass() );
    }   //  IllegalAnnotationError()

    /**
     *  Creates a new {@code IllegalAnnotationError} instance.
     *
     *  @param  message The message for the error.
     *  @param  annotationClass The class of the invalid annotation.
     */
    public IllegalAnnotationError( final String message, final Class<? extends Annotation> annotationClass )
    {
        this( "Annotation '%s' is illegal: %s".formatted( requireNonNullArgument( annotationClass, "annotationClass" ).getName(), requireNonNullArgument( message, "message" ) ) );
    }   //  IllegalAnnotationError()

    /**
     *  Creates a new {@code IllegalAnnotationError} instance.
     *
     *  @param  message The message for the error.
     *  @param  cause   The root exception for this error.
     */
    public IllegalAnnotationError( final String message, final Throwable cause ) { super( requireNonNullArgument( message, "message" ), cause ); }
}
//  class IllegalAnnotationError

/*
 *  End of File
 */