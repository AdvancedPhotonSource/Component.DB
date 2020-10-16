/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.import_export.import_.objects.specs;

import gov.anl.aps.cdb.portal.import_export.import_.objects.handlers.StringInputHandler;
import gov.anl.aps.cdb.portal.import_export.import_.objects.handlers.InputHandler;

/**
 *
 * @author craig
 */
public class StringColumnSpec extends ColumnSpec {

    private int maxLength;

    public StringColumnSpec(
            String header, String propertyName, String entitySetterMethod, boolean required, String description, int maxLength) {

        super(header, propertyName, entitySetterMethod, required, description);
        this.maxLength = maxLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public InputHandler getInputHandler(int colIndex) {
        return new StringInputHandler(
                colIndex,
                getHeader(),
                getPropertyName(),
                getEntitySetterMethod(),
                getMaxLength());
    }
}
