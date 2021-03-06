#!/usr/bin/env python

"""
Copyright (c) UChicago Argonne, LLC. All rights reserved.
See LICENSE file.
"""


#
# Internal error class.
#

#######################################################################

from cdb.common.constants import cdbStatus 
from cdb.common.exceptions.cdbException import CdbException

#######################################################################

class InternalError(CdbException):
    def __init__ (self, error='', **kwargs):
        CdbException.__init__(self, error, cdbStatus.CDB_INTERNAL_ERROR, **kwargs)
