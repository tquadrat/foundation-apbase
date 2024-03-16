/*
 * ============================================================================
 * Copyright © 2002-2024 by Thomas Thrien.
 * All Rights Reserved.
 * ============================================================================
 *
 * Licensed to the public under the agreements of the GNU Lesser General Public
 * License, version 3.0 (the "License"). You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/lgpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.tquadrat.foundation.ap;

import org.apiguardian.api.API;
import org.tquadrat.foundation.annotation.ClassVersion;

import java.io.Serial;

import static org.apiguardian.api.API.Status.STABLE;
import static org.tquadrat.foundation.lang.Objects.requireNonNullArgument;

/**
 *  The error that will be thrown when there is a general problem with the
 *  annotation processing.
 *
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @version $Id: AnnotationProcessingError.java 1117 2024-03-15 15:13:48Z tquadrat $
 *  @since 0.1.0
 *
 *  @UMLGraph.link
 */
@ClassVersion( sourceVersion = "$Id: AnnotationProcessingError.java 1117 2024-03-15 15:13:48Z tquadrat $" )
@API( status = STABLE, since = "0.1.0" )
public sealed class AnnotationProcessingError extends Error
    permits CodeGenerationError, IllegalAnnotationError
{
        /*------------------------*\
    ====** Static Initialisations **===========================================
        \*------------------------*/
    /**
     *  The serial version UID for objects of this class: {@value}.
     *
     *  @hidden
     */
    @Serial
    private static final long serialVersionUID = 1L;

        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  Constructs a new error with {@code null} as its detail message. The
     *  cause is not initialised, and may subsequently be initialised by a call
     *  to
     *  {@link #initCause}.
     */
    public AnnotationProcessingError() { super(); }

    /**
     *  Constructs a new error with the specified detail message. The cause is
     *  not initialised, and may subsequently be initialised by a call to
     *  {@link #initCause}.
     *
     *  @param  message The detail message. It is saved for later retrieval by
     *      the
     *      {@link #getMessage()}
     *      method.
     */
    public AnnotationProcessingError( final String message ) { super( requireNonNullArgument( message, "message" ) ); }

    /**
     *  <p>{@summary Constructs a new error with the specified detail message
     *  and cause.}</p>
     *  <p>Note that the detail message associated with {@code cause} is
     *  <i>not</i> automatically incorporated in this error's detail
     *  message.</p>
     *
     *  @param  message The detail message (which is saved for later retrieval
     *      by the
     *      {@link #getMessage()}
     *      method).
     *  @param  cause   The cause (which is saved for later retrieval by the
     *      {@link #getCause()}
     *      method). A {@code null} value is permitted, and indicates that the
     *      cause is nonexistent or unknown.
     */
    public AnnotationProcessingError( final String message, final Throwable cause ) { super( requireNonNullArgument( message, "message" ), cause ); }

    /**
     *  <p>{@summary Constructs a new error with the specified cause and a
     *  detail message of {@code (cause==null ? null : cause.toString())}
     *  (which typically contains the class and detail message of
     *  {@code cause}).}</p>
     *  <p>This constructor is useful for errors that are little more than
     *  wrappers for other instances of
     *  {@link Throwable}.</p>
     *
     *  @param  cause   The cause (which is saved for later retrieval by the
     *      {@link #getCause()}
     *      method). A {@code null} value is permitted, and indicates that the
     *      cause is nonexistent or unknown.
     */
    public AnnotationProcessingError( final Throwable cause ) { super( cause ); }
}
//  class AnnotationProcessingError

/*
 *  End of File
 */