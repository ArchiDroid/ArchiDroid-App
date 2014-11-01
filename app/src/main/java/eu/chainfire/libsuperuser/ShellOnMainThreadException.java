/*
 * ========================================================================
 *     _             _     _ ____            _     _
 *    / \   _ __ ___| |__ (_)  _ \ _ __ ___ (_) __| |
 *   / _ \ | '__/ __| '_ \| | | | | '__/ _ \| |/ _` |
 *  / ___ \| | | (__| | | | | |_| | | | (_) | | (_| |
 * /_/   \_\_|  \___|_| |_|_|____/|_|  \___/|_|\__,_|
 *
 * Copyright 2014 Łukasz "JustArchi" Domeradzki
 * Contact: JustArchi@JustArchi.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE=2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
 */

package eu.chainfire.libsuperuser;

/**
 * Exception class used to crash application when shell commands are executed
 * from the main thread, and we are in debug mode. 
 */
@SuppressWarnings("serial")
public class ShellOnMainThreadException extends RuntimeException {
    public static final String EXCEPTION_COMMAND = "Application attempted to run a shell command from the main thread";
    public static final String EXCEPTION_NOT_IDLE = "Application attempted to wait for a non-idle shell to close on the main thread";
    public static final String EXCEPTION_WAIT_IDLE = "Application attempted to wait for a shell to become idle on the main thread";

    public ShellOnMainThreadException(String message) {
        super(message);
    }
}
