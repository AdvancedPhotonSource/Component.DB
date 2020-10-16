/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.import_export.import_.objects.specs;

import gov.anl.aps.cdb.portal.import_export.import_.objects.ColumnSpecInitInfo;
import gov.anl.aps.cdb.portal.import_export.import_.objects.InputColumnModel;
import gov.anl.aps.cdb.portal.import_export.import_.objects.OutputColumnModel;
import gov.anl.aps.cdb.portal.import_export.import_.objects.ValidInfo;
import gov.anl.aps.cdb.portal.import_export.import_.objects.handlers.InputHandler;
import java.util.List;
import java.util.Map;

/**
 *
 * @author craig
 */
public class IgnoreColumnSpec extends ColumnSpec {
    
    private int numIgnoreColumns = 0;
    
    public IgnoreColumnSpec(int numColumns) {
        super();
        this.numIgnoreColumns = numColumns;
    }
    
    @Override
    public ColumnSpecInitInfo initialize(
            int colIndex,
            Map<Integer, String> headerValueMap,
            List<InputColumnModel> inputColumns_io,
            List<InputHandler> inputHandlers_io,
            List<OutputColumnModel> outputColumns_io) {
        
        ValidInfo validInfo = new ValidInfo(true, "");
        return new ColumnSpecInitInfo(validInfo, numIgnoreColumns);
    }
    
    @Override
    public InputHandler getInputHandler(int colIndex) {
        return null;
    }
}
