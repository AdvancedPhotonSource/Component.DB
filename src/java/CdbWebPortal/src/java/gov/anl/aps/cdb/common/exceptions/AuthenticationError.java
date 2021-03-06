/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.common.exceptions;

import gov.anl.aps.cdb.common.constants.CdbStatus;

/**
 * Authentication error exception.
 */
public class AuthenticationError extends CdbException {

    /**
     * Default constructor.
     */
    public AuthenticationError() {
        super();
    }

    /**
     * Constructor using error message.
     *
     * @param message error message
     */
    public AuthenticationError(String message) {
        super(message);
    }

    /**
     * Constructor using throwable object.
     *
     * @param throwable throwable object
     */
    public AuthenticationError(Throwable throwable) {
        super(throwable);
    }

    /**
     * Constructor using error message and throwable object.
     *
     * @param message error message
     * @param throwable throwable object
     */
    public AuthenticationError(String message, Throwable throwable) {
        super(message, throwable);
    }

    @Override
    public int getErrorCode() {
        return CdbStatus.CDB_AUTHENTICATION_ERROR;
    }    
}
