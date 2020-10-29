#
# This file contains a pre-import framework for various CDB object types.  It supports reading an input spreadsheet
# file, and then creating an output spreadsheet file that contains the standard columns for CDB import.  It is intended
# to support the cable data collection process, which uses an Excel workbook to collect cable type and cable data from
# users.  The pre-import process takes that data collection workbook, and augments/formats it for import to CDB.
#
# The framework initially supports pre-import data transformation for 3 types of objects: sources, cable types, and
# cable designs.  To add support for a new type of object, you must provide concrete subclass implementations of the
# framework's PreImportHelper and OutputObject bases classes.  For an example, see the classes "SourceHelper" and
# SourceOutputObject, which together support the pre-import transformation of CDB "Source" objects.
#
# The framework opens the input spreadsheet and processes it row by row.  A python dictionary is created for each
# input row.  The helper class validates and filters the input rows.  The framework collects a list of "OutputObject"
# instances, and then iterates through them one by one, writing a row for each to the output spreadsheet.
#
# The roles of the framework classes that must be extended to add pre-import support for a new object type are as
# follows.
#
# The PreImportHelper subclass specifies the input spreadsheet columns via the input_column_list() method,
# including the column index and dictionary key name to use for each.  Not all input columns need be mapped,
# only those that are used to generate the values in the output objects.  Column labels are not needed in the input
# column specifications since we don't use them.  It also specifies the columns for the output spreadsheet via the
# output_column_list() method, including for each a column index, label, and getter method name on the output object
# to extract the column value.  It specifies the number of columns in the input spreadsheet (num_input_columns()) and
# the number of columns in the output spreadsheet (num_output_columns()).  It implements get_output_object() which
# takes the dictionary of values for a row from the input spreadsheet and produces an instance of the OutputObject
# subclass that will be used to generate a row in the output spreadsheet.  Finally, the tag() function returns the
# value that identifies the pre-import object type, e.g., "Source".  This value must match the "--type" command line
# option.
#
# The OutputObject subclass's primary responsibility is to transform the data read from an input spreadsheet row into
# the values needed for the corresponding row in the output spreadsheet.  Sometimes the output value is simply a copy of
# the input value, but in other cases, we might perform a CDB query API lookup to transform a name from the input file
# to the corresponding database identifier for that name in the output file.  Each getter method in the OutputObject
# corresponds to a column in the output spreadsheet.  It is these getter methods that perform the required data
# transformation, such as API query.  The column specifications in the helper's output_column_list() map the column
# index in the output spreadsheet to the appropriate getter method on the output object.

# Input spreadsheet format assumptions:
#   * contains a single worksheet
#   * contains 2 or more rows, header is on row 1, data starts on row 2
#   * there are no empty rows within the data


import argparse
import logging
import sys
from abc import ABC, abstractmethod
import re

import xlrd
import xlsxwriter

from CdbApiFactory import CdbApiFactory
from cdbApi import ApiException


# constants

CABLE_TYPE_NAME_KEY = "name"
CABLE_TYPE_ALT_NAME_KEY = "altName"
CABLE_TYPE_DESCRIPTION_KEY = "description"
CABLE_TYPE_LINK_URL_KEY = "linkUrl"
CABLE_TYPE_IMAGE_URL_KEY = "imageUrl"
CABLE_TYPE_MANUFACTURER_KEY = "manufacturer"
CABLE_TYPE_PART_NUMBER_KEY = "partNumber"
CABLE_TYPE_ALT_PART_NUMBER_KEY = "altPartNumber"
CABLE_TYPE_DIAMETER_KEY = "diameter"
CABLE_TYPE_WEIGHT_KEY = "weight"
CABLE_TYPE_CONDUCTORS_KEY = "conductors"
CABLE_TYPE_INSULATION_KEY = "insulation"
CABLE_TYPE_JACKET_COLOR_KEY = "jacketColor"
CABLE_TYPE_VOLTAGE_RATING_KEY = "voltageRating"
CABLE_TYPE_FIRE_LOAD_KEY = "fireLoad"
CABLE_TYPE_HEAT_LIMIT_KEY = "heatLimit"
CABLE_TYPE_BEND_RADIUS_KEY = "bendRadius"
CABLE_TYPE_RAD_TOLERANCE_KEY = "radTolerance"
CABLE_TYPE_TOTAL_LENGTH_KEY = "totalLength"
CABLE_TYPE_REEL_LENGTH_KEY = "reelLength"
CABLE_TYPE_REEL_QTY_KEY = "reelQty"
CABLE_TYPE_LEAD_TIME_KEY = "leadTime"
CABLE_TYPE_ORDERED_KEY = "ordered"
CABLE_TYPE_RECEIVED_KEY = "received"

CABLE_INVENTORY_NAME_KEY = "name"

CABLE_DESIGN_NAME_KEY = "name"
CABLE_DESIGN_LAYING_KEY = "laying"
CABLE_DESIGN_VOLTAGE_KEY = "voltage"
CABLE_DESIGN_OWNER_KEY = "owner"
CABLE_DESIGN_TYPE_KEY = "type"
CABLE_DESIGN_SRC_LOCATION_KEY = "srcLocation"
CABLE_DESIGN_SRC_ANS_KEY = "srcANS"
CABLE_DESIGN_SRC_ETPM_KEY = "srcETPM"
CABLE_DESIGN_SRC_ADDRESS_KEY = "srcAddress"
CABLE_DESIGN_SRC_DESCRIPTION_KEY = "srcDescription"
CABLE_DESIGN_DEST_LOCATION_KEY = "destLocation"
CABLE_DESIGN_DEST_ANS_KEY = "destANS"
CABLE_DESIGN_DEST_ETPM_KEY = "destETPM"
CABLE_DESIGN_DEST_ADDRESS_KEY = "destAddress"
CABLE_DESIGN_DEST_DESCRIPTION_KEY = "destDescription"
CABLE_DESIGN_LEGACY_ID_KEY = "legacyId"
CABLE_DESIGN_FROM_DEVICE_NAME_KEY = "fromDeviceName"
CABLE_DESIGN_TO_DEVICE_NAME_KEY = "toDeviceName"
CABLE_DESIGN_MBA_ID_KEY = "mbaId"
CABLE_DESIGN_IMPORT_ID_KEY = "importId"
CABLE_DESIGN_QR_ID_KEY = "qrId"
CABLE_DESIGN_VIA_ROUTE_KEY = "via"
CABLE_DESIGN_WAYPOINT_ROUTE_KEY = "waypoint"

name_manager = None


def register(helper_class):
    PreImportHelper.register(helper_class.tag(), helper_class)


class PreImportHelper(ABC):

    helperDict = {}

    def __init__(self):
        self.input_columns = {}
        self.output_columns = {}
        self.args = None
        self.api = None

    # registers helper concrete classes for lookup by tag
    @classmethod
    def register(cls, tag, the_class):
        cls.helperDict[tag] = the_class

    # returns helper class for specified tag
    @classmethod
    def get_helper_class(cls, tag):
        return cls.helperDict[tag]

    # checks if specified tag is valid
    @classmethod
    def is_valid_type(cls, tag):
        return tag in cls.helperDict

    # creates instance of class with specified tag
    @classmethod
    def create_helper(cls, tag):
        helper_class = cls.helperDict[tag]
        helper_instance = helper_class()
        return helper_instance

    # Allows subclasses to add command line parser args.  Default behavior is to do nothing.
    # e.g., "parser.add_argument("--cdbUser", help="CDB User ID for API login", required=True)"
    @staticmethod
    def add_parser_args(parser):
        pass

    # Returns registered tag for subclass.
    @staticmethod
    @abstractmethod  # must be innermost decorator
    def tag():
        pass

    # Returns expected number of columns in input spreadsheet.
    @abstractmethod
    def num_input_cols(self):
        pass

    def num_output_cols(self):
        return len(self.output_column_list())

    # Returns list of column models for input spreadsheet.  Not all columns need be mapped, only the ones we wish to
    # read values from.
    @abstractmethod
    def input_column_list(self):
        pass

    # Returns list of column models for output spreadsheet.  Not all columns need be mapped, only the ones we wish to
    # write values to.
    @abstractmethod
    def output_column_list(self):
        pass

    # Returns list of input handlers.
    @abstractmethod
    def input_handler_list(self):
        pass

    # Returns an output object for the specified input object, or None if the input object is duplicate.
    @abstractmethod
    def get_output_object(self, input_dict):
        pass

    def set_args(self, args):
        self.args = args

    def get_args(self):
        return self.args

    def set_api(self, api):
        self.api = api

    # Builds dictionary whose keys are column index and value is column model object.
    def initialize_input_columns(self):
        for col in self.input_column_list():
            self.input_columns[col.index] = col

    # Builds dictionary whose keys are column index and value is column model object.
    def initialize_output_columns(self):
        for col in self.output_column_list():
            self.output_columns[col.index] = col

    # Handles cell value from input spreadsheet at specified column index for supplied input object.
    def handle_input_cell_value(self, input_dict, index, value, row_num):
        key = self.input_columns[index].key
        input_dict[key] = value

    def input_row_is_empty(self, input_dict, row_num):
        non_empty_count = sum([1 for val in input_dict.values() if len(str(val)) > 0])
        if non_empty_count == 0 or self.input_row_is_empty_custom(input_dict, row_num):
            logging.debug("skipping empty row: %d" % row_num)
            return True

    # Returns True if the row represented by input_dict should be treated as a blank row.  Default is False.  Subclass
    # can override to allow certain non-empty values to be treated as empty.
    def input_row_is_empty_custom(self, input_dict, row_num):
        return False

    # Performs validation on row from input spreadsheet and returns True if the row is determined to be valid.
    # Can return False where input is valid, but it might be better to call sys.exit() with a useful message.
    def input_row_is_valid(self, input_dict, row_num):

        is_valid = True
        valid_messages = []

        for column in self.input_column_list():

            required = column.required
            if required:
                value = input_dict[column.key]
                if value is None or len(str(value)) == 0:
                    is_valid = False
                    valid_messages.append("required value missing for key: %s row index: %d" % (column.key, row_num))

        (custom_is_valid, custom_valid_string) = self.input_row_is_valid_custom(input_dict)
        if not custom_is_valid:
            is_valid = False
            valid_messages.append(custom_valid_string)

        return is_valid, valid_messages

    # Performs custom validation on input row.  Returns True if row is valid.  Default is to return True. Subclass
    # can override to customize.
    def input_row_is_valid_custom(self, input_dict):
        return True, ""

    def invoke_row_handlers(self, input_dict, row_num):

        is_valid = True
        valid_messages = []

        for handler in self.input_handler_list():
            (handler_is_valid, handler_valid_string) = handler.handle_input(input_dict)
            if not handler_is_valid:
                is_valid = False
                valid_messages.append(handler_valid_string)

        return is_valid, valid_messages

    # Returns column label for specified column index.
    def get_output_column_label(self, col_index):
        return self.output_columns[col_index].label

    # Returns value for output spreadsheet cell and supplied object at specified index.
    def get_output_cell_value(self, obj, index):
        # use reflection to invoke column getter method on supplied object
        val = getattr(obj, self.output_columns[index].method)()
        logging.debug("index: %d method: %s value: %s" % (index, self.output_columns[index].method, val))
        return val

    # Complete helper processing after all output objects are processed.  Subclass overrides to customize.
    def close(self):
        pass


class ConnectedMenuManager:

    def __init__(self, workbook):
        self.name_dict = {}
        self.initialize(workbook)

    def add_name(self, name, values):
        self.name_dict[name] = values

    def initialize(self, workbook):

        for name in workbook.name_obj_list:
            if name.scope == -1:
                # print(name.name)
                name.book = workbook  # seems like an error that library doesn't do this internally
                (sheet_name, ref) = name.formula_text.split('!')
                if ':' in ref:
                    (first_cell, last_cell) = ref.split(':')
                else:
                    first_cell = ref
                    last_cell = ref
                (first_cell_row, first_cell_col) = xl_cell_to_rowcol(first_cell)
                (last_cell_row, last_cell_col) = xl_cell_to_rowcol(last_cell)
                sheet = workbook.sheet_by_name(sheet_name)
                values = []
                for row_ind in range(first_cell_row, last_cell_row + 1):
                    for col_ind in range(first_cell_col, last_cell_col + 1):
                        values.append(sheet.cell(row_ind, col_ind).value)
                        # print("\t%s" % sheet.cell(row_ind, col_ind).value)
                self.add_name(name.name, values)

    def has_name(self, range_name):
        return range_name in self.name_dict

    def value_is_valid_for_name(self, parent_value, child_value):
        child_value_list = self.name_dict.get(parent_value)
        if child_value_list is not None:
            return child_value in child_value_list
        else:
            return False


class InputHandler(ABC):

    def __init__(self, column_key):
        self.column_key = column_key

    # Invokes handler.
    @abstractmethod
    def handle_input(self, input_dict):
        pass


class ManufacturerHandler(InputHandler):

    def __init__(self, column_key, api, existing_sources, new_sources):
        super().__init__(column_key)
        self.existing_sources = existing_sources
        self.new_sources = new_sources
        self.api = api

    def handle_input(self, input_dict):

        manufacturer = input_dict[self.column_key].upper()

        if len(manufacturer) == 0:
            # no manufacturer specified, skip
            return True, ""

        if (manufacturer.upper in self.new_sources) or (manufacturer in self.existing_sources):
            # already looked up this manufacturer
            return True, ""

        # check to see if manufacturer exists as a CDB Source
        try:
            mfr_source = self.api.getSourceApi().get_source_by_name(manufacturer)
        except ApiException as ex:
            if "ObjectNotFound" not in ex.body:
                msg = "exception retrieving source for manufacturer: %s - %s" % (manufacturer, ex.body)
                logging.error(msg)
                print(msg)
                return False, msg
            mfr_source = None
        if mfr_source:
            # source already exists for mfr
            self.existing_sources.append(manufacturer)

        else:
            # need to add new source for mfr
            self.new_sources.append(manufacturer)

        return True, ""


class ConnectedMenuHandler(InputHandler):

    def __init__(self, column_key, parent_key):
        super().__init__(column_key)
        self.parent_key = parent_key

    def handle_input(self, input_dict):
        global name_manager
        parent_value = input_dict[self.parent_key]
        cell_value = input_dict[self.column_key]
        if not name_manager.has_name(parent_value):
            sys.exit("name manager has no menu range for: %s" % parent_value)
        has_child = name_manager.value_is_valid_for_name(parent_value, cell_value)
        valid_string = ""
        if not has_child:
            valid_string = "range for parent name %s does not include child name %s" % (parent_value, cell_value)
        return has_child, valid_string


class NamedRangeHandler(InputHandler):

    def __init__(self, column_key, range_name):
        super().__init__(column_key)
        self.range_name = range_name

    def handle_input(self, input_dict):
        global name_manager
        if not name_manager.has_name(self.range_name):
            sys.exit("name manager has no named range for: %s" % self.range_name)
        cell_value = input_dict[self.column_key]
        has_child = name_manager.value_is_valid_for_name(self.range_name, cell_value)
        valid_string = ""
        if not has_child:
            valid_string = "named range %s does not include value %s" % (self.range_name, cell_value)
        return has_child, valid_string


class DeviceAddressHandler(InputHandler):

    def __init__(self, column_key, location_key, etpm_key):
        super().__init__(column_key)
        self.location_key = location_key
        self.etpm_key = etpm_key

    def handle_input(self, input_dict):

        global name_manager

        location_value = input_dict[self.location_key]
        etpm_value = input_dict[self.etpm_key]
        cell_value = input_dict[self.column_key]

        range_name = ""
        if location_value == "SR_T":
            range_name = "Snn" + etpm_value[3:]
        elif "PS-" in etpm_value:
            range_name = "_PS_CAB_SLOT_"
        else:
            range_name = "_RACK_AREA_"

        if not name_manager.has_name(range_name):
            sys.exit("name manager has no named address range for: %s" % range_name)

        has_child = name_manager.value_is_valid_for_name(range_name, cell_value)
        valid_string = ""
        if not has_child:
            valid_string = "named address range %s does not include value %s" % (range_name, cell_value)
        return has_child, valid_string


class EndpointHandler(InputHandler):

    def __init__(self, column_key, rack_key, hierarchy_name, api, rack_manager, missing_endpoints, nonunique_endpoints):
        super().__init__(column_key)
        self.rack_key = rack_key
        self.hierarchy_name = hierarchy_name
        self.api = api
        self.rack_manager = rack_manager
        self.missing_endpoints = missing_endpoints
        self.nonunique_endpoints = nonunique_endpoints

    def handle_input(self, input_dict):

        endpoint_name = input_dict[self.column_key]
        rack_name = input_dict[self.rack_key]

        # check endpoint cache before calling API
        if self.rack_manager.get_endpoint_id_for_rack(rack_name, endpoint_name) is not None:
            logging.info("found endpoint: %s for rack: %s in cache")
            return True, ""

        result_list = []
        try:
            result_list = self.api.getMachineDesignItemApi().get_md_in_hierarchy_by_name(self.hierarchy_name, rack_name, endpoint_name)
        except ApiException as ex:
            return False, "exception invoking cdb endpoint retrieval api"

        is_valid = True
        valid_string = ""

        if len(result_list) == 0:
            is_valid = False
            valid_string = "no endpoint item found with name: %s rack: %s in hierarchy: %s" % (endpoint_name, rack_name, self.hierarchy_name)
            logging.error(valid_string)
            self.missing_endpoints.add(rack_name + " + " + endpoint_name)

        elif len(result_list) > 1:
            is_valid = False
            valid_string = "duplicate endpoint items found with name: %s rack: %s in hierarchy: %s" % (endpoint_name, rack_name, self.hierarchy_name)
            logging.error(valid_string)
            self.nonunique_endpoints.add(rack_name + " + " + endpoint_name)

        else:
            endpoint_object = result_list[0]
            endpoint_id = endpoint_object.id
            self.rack_manager.add_endpoint_id_for_rack(rack_name, endpoint_name, endpoint_id)
            logging.debug("found machine design item with name: %s, id: %s" % (endpoint_name, endpoint_id))

        return is_valid, valid_string


class CableTypeHandler(InputHandler):

    def __init__(self, column_key, id_manager, api, missing_cable_type_list):
        super().__init__(column_key)
        self.id_manager = id_manager
        self.api = api
        self.missing_cable_type_list = missing_cable_type_list

    def handle_input(self, input_dict):

        cable_type_name = input_dict[self.column_key]

        cached_id = self.id_manager.get_id_for_name(cable_type_name)
        if cached_id is not None:
            # nothing to do, id already cached
            return True, ""

        else:
            # check to see if cable type exists in CDB by name
            cable_type_object = None
            try:
                cable_type_object = self.api.getCableCatalogItemApi().get_cable_catalog_item_by_name(cable_type_name)
            except ApiException as ex:
                if "ObjectNotFound" not in ex.body:
                    error_msg = "unknown api exception retrieving cable catalog item: %s - %s" % (cable_type_name, ex.body)
                    logging.error(error_msg)
                    return False, error_msg

            if cable_type_object:
                cable_type_id = cable_type_object.id
                logging.debug("found cable type with name: %s, id: %s" % (cable_type_name, cable_type_id))
                self.id_manager.set_id_for_name(cable_type_name, cable_type_id)
                return True, ""
            else:
                self.missing_cable_type_list.add(cable_type_name)
                error_msg = "no cable type found for name: %s" % cable_type_name
                logging.error(error_msg)
                return False, error_msg


class SourceHandler(InputHandler):

    def __init__(self, column_key, id_manager, api, missing_source_list):
        super().__init__(column_key)
        self.id_manager = id_manager
        self.api = api
        self.missing_source_list = missing_source_list

    def handle_input(self, input_dict):

        source_name = input_dict[self.column_key]

        if source_name == "" or source_name is None:
            return True, ""

        cached_id = self.id_manager.get_id_for_name(source_name)
        if cached_id is not None:
            # nothing to do, id already cached
            return True, ""

        else:
            # check to see if source exists in CDB by name
            source_object = None
            try:
                source_object = self.api.getSourceApi().get_source_by_name(source_name)
            except ApiException as ex:
                if "ObjectNotFound" not in ex.body:
                    error_msg = "unknown api exception retrieving source item: %s - %s" % (source_name, ex.body)
                    logging.error(error_msg)
                    return False, error_msg

            if source_object:
                source_id = source_object.id
                logging.debug("found source with name: %s, id: %s" % (source_name, source_id))
                self.id_manager.set_id_for_name(source_name, source_id)
                return True, ""
            else:
                self.missing_source_list.append(source_name)
                error_msg = "no source found for name: %s" % source_name
                logging.error(error_msg)
                return False, error_msg
            

class InputColumnModel:

    def __init__(self, col_index, key, validator=None, required=False):
        self.index = col_index
        self.key = key
        self.required = required


class OutputColumnModel:

    def __init__(self, col_index, method, label=""):
        self.index = col_index
        self.method = method
        self.label = label


class OutputObject(ABC):

    def __init__(self, helper, input_dict):
        self.helper = helper
        self.input_dict = input_dict


class IdManager():

    def __init__(self):
        self.name_id_dict = {}

    def set_id_for_name(self, name, id):
        self.name_id_dict[name] = id

    def get_id_for_name(self, name):
        if name in self.name_id_dict:
            return self.name_id_dict[name]
        else:
            return None


class NameVariantManager():

    def __init__(self):
        self.name_list = []

    def add_name(self, name):
        is_valid = True
        valid_string = ""
        for n in self.name_list:
            if n == name:
                return True, ""
            elif n.upper() == name.upper():
                is_valid = False
                valid_string = "%s is variation of existing name %s" % (name, n)
        self.name_list.append(name)
        return is_valid, valid_string


class RackManager():

    def __init__(self):
        self.rack_dict = {}

    def add_endpoint_id_for_rack(self, rack_name, endpoint_name, endpoint_id):
        if not rack_name in self.rack_dict:
            self.rack_dict[rack_name] = {}
        rack_contents = self.rack_dict[rack_name]
        rack_contents[endpoint_name] = endpoint_id

    def get_endpoint_id_for_rack(self, rack_name, endpoint_name):
        if rack_name in self.rack_dict:
            rack_items = self.rack_dict[rack_name]
            if endpoint_name in rack_items:
                return rack_items[endpoint_name]
        return None


@register
class SourceHelper(PreImportHelper):

    def __init__(self):
        super().__init__()
        self.output_manufacturers = set()
        self.source_name_manager = NameVariantManager()
        self.existing_sources = []
        self.new_sources = []

    @staticmethod
    def tag():
        return "Source"

    def num_input_cols(self):
        return 23

    def input_column_list(self):
        column_list = [
            InputColumnModel(col_index=2, key=CABLE_TYPE_MANUFACTURER_KEY),
        ]
        return column_list

    def output_column_list(self):
        column_list = [
            OutputColumnModel(col_index=0, method="get_name", label="Name"),
            OutputColumnModel(col_index=1, method="get_description", label="Description"),
            OutputColumnModel(col_index=2, method="get_contact_info", label="Contact Info"),
            OutputColumnModel(col_index=3, method="get_url", label="URL"),
        ]
        return column_list

    def input_handler_list(self):
        global name_manager
        handler_list = [
            ManufacturerHandler(CABLE_TYPE_MANUFACTURER_KEY, self.api, self.existing_sources, self.new_sources),
        ]
        return handler_list

    def get_output_object(self, input_dict):

        manufacturer = input_dict[CABLE_TYPE_MANUFACTURER_KEY].upper()

        if len(manufacturer) == 0:
            logging.debug("manufacturer is empty")
            return None

        logging.debug("found manufacturer: %s" % manufacturer)

        if manufacturer in self.existing_sources:
            # don't need to import this item
            logging.debug("skipping existing cdb manufacturer: %s" % manufacturer)
            return None

        if manufacturer not in self.output_manufacturers:
            # need to write this object to output spreadsheet for import
            self.output_manufacturers.add(manufacturer)
            logging.debug("adding new manufacturer: %s to output" % manufacturer)
            return SourceOutputObject(helper=self, input_dict=input_dict)

        else:
            # already added to output spreadsheet
            logging.debug("ignoring duplicate manufacturer: %s" % manufacturer)
            return None

    def close(self):
        if len(self.new_sources) > 0 or len(self.existing_sources) > 0:

            if not self.args.infoFile:
                print("provide command line arg 'infoFile' to generate debugging output file")
                return

            output_book = xlsxwriter.Workbook(self.args.infoFile)
            output_sheet = output_book.add_worksheet()

            output_sheet.write(0, 0, "new sources")
            output_sheet.write(0, 1, "existing cdb sources")

            row_index = 1
            for src in self.new_sources:
                output_sheet.write(row_index, 0, src)
                row_index = row_index + 1

            row_index = 1
            for src in self.existing_sources:
                output_sheet.write(row_index, 1, src)
                row_index = row_index + 1

            output_book.close()


class SourceOutputObject(OutputObject):

    def __init__(self, helper, input_dict):
        super().__init__(helper, input_dict)
        self.description = ""
        self.contact_info = ""
        self.url = ""

    def get_name(self):
        return self.input_dict[CABLE_TYPE_MANUFACTURER_KEY]

    def get_description(self):
        return self.description

    def get_contact_info(self):
        return self.contact_info

    def get_url(self):
        return self.url


@register
class CableTypeHelper(PreImportHelper):

    def __init__(self):
        super().__init__()
        self.source_id_manager = IdManager()
        self.missing_source_list = []

    # Adds helper specific command line args.
    # e.g., "parser.add_argument("--cdbUser", help="CDB User ID for API login", required=True)"
    @staticmethod
    def add_parser_args(parser):
        parser.add_argument("--projectId", help="CDB item category ID for project (item_project table)", required=True)
        parser.add_argument("--techSystemId", help="CDB technical system ID for owner", required=True)
        parser.add_argument("--ownerUserId", help="CDB user ID for owner", required=True)
        parser.add_argument("--ownerGroupId", help="CDB group ID for owner", required=True)

    def set_args(self, args):
        super().set_args(args)
        print("CDB project id: %s" % args.projectId)
        print("CDB technical system id: %s" % args.techSystemId)
        print("CDB owner user id: %s" % args.ownerUserId)
        print("CDB owner group id: %s" % args.ownerGroupId)

    @staticmethod
    def tag():
        return "CableType"

    def num_input_cols(self):
        return 23

    def input_column_list(self):
        column_list = [
            InputColumnModel(col_index=0, key=CABLE_TYPE_NAME_KEY, required=True),
            InputColumnModel(col_index=1, key=CABLE_TYPE_DESCRIPTION_KEY),
            InputColumnModel(col_index=2, key=CABLE_TYPE_MANUFACTURER_KEY),
            InputColumnModel(col_index=3, key=CABLE_TYPE_PART_NUMBER_KEY),
            InputColumnModel(col_index=4, key=CABLE_TYPE_ALT_PART_NUMBER_KEY),
            InputColumnModel(col_index=5, key=CABLE_TYPE_DIAMETER_KEY),
            InputColumnModel(col_index=6, key=CABLE_TYPE_WEIGHT_KEY),
            InputColumnModel(col_index=7, key=CABLE_TYPE_CONDUCTORS_KEY),
            InputColumnModel(col_index=8, key=CABLE_TYPE_INSULATION_KEY),
            InputColumnModel(col_index=9, key=CABLE_TYPE_JACKET_COLOR_KEY),
            InputColumnModel(col_index=10, key=CABLE_TYPE_VOLTAGE_RATING_KEY),
            InputColumnModel(col_index=11, key=CABLE_TYPE_FIRE_LOAD_KEY),
            InputColumnModel(col_index=12, key=CABLE_TYPE_HEAT_LIMIT_KEY),
            InputColumnModel(col_index=13, key=CABLE_TYPE_BEND_RADIUS_KEY),
            InputColumnModel(col_index=14, key=CABLE_TYPE_RAD_TOLERANCE_KEY),
            InputColumnModel(col_index=15, key=CABLE_TYPE_LINK_URL_KEY),
            InputColumnModel(col_index=16, key=CABLE_TYPE_IMAGE_URL_KEY),
            InputColumnModel(col_index=17, key=CABLE_TYPE_TOTAL_LENGTH_KEY),
            InputColumnModel(col_index=18, key=CABLE_TYPE_REEL_LENGTH_KEY),
            InputColumnModel(col_index=19, key=CABLE_TYPE_REEL_QTY_KEY),
            InputColumnModel(col_index=20, key=CABLE_TYPE_LEAD_TIME_KEY),
            InputColumnModel(col_index=21, key=CABLE_TYPE_ORDERED_KEY),
            InputColumnModel(col_index=22, key=CABLE_TYPE_RECEIVED_KEY),
        ]
        return column_list

    def output_column_list(self):
        column_list = [
            OutputColumnModel(col_index=0, method="get_name", label=CABLE_TYPE_NAME_KEY),
            OutputColumnModel(col_index=1, method="get_alt_name", label=CABLE_TYPE_ALT_NAME_KEY),
            OutputColumnModel(col_index=2, method="get_description", label=CABLE_TYPE_DESCRIPTION_KEY),
            OutputColumnModel(col_index=3, method="get_link_url", label=CABLE_TYPE_LINK_URL_KEY),
            OutputColumnModel(col_index=4, method="get_image_url", label=CABLE_TYPE_IMAGE_URL_KEY),
            OutputColumnModel(col_index=5, method="get_manufacturer_id", label=CABLE_TYPE_MANUFACTURER_KEY),
            OutputColumnModel(col_index=6, method="get_part_number", label=CABLE_TYPE_PART_NUMBER_KEY),
            OutputColumnModel(col_index=7, method="get_alt_part_number", label=CABLE_TYPE_ALT_PART_NUMBER_KEY),
            OutputColumnModel(col_index=8, method="get_diameter", label=CABLE_TYPE_DIAMETER_KEY),
            OutputColumnModel(col_index=9, method="get_weight", label=CABLE_TYPE_WEIGHT_KEY),
            OutputColumnModel(col_index=10, method="get_conductors", label=CABLE_TYPE_CONDUCTORS_KEY),
            OutputColumnModel(col_index=11, method="get_insulation", label=CABLE_TYPE_INSULATION_KEY),
            OutputColumnModel(col_index=12, method="get_jacket_color", label=CABLE_TYPE_JACKET_COLOR_KEY),
            OutputColumnModel(col_index=13, method="get_voltage_rating", label=CABLE_TYPE_VOLTAGE_RATING_KEY),
            OutputColumnModel(col_index=14, method="get_fire_load", label=CABLE_TYPE_FIRE_LOAD_KEY),
            OutputColumnModel(col_index=15, method="get_heat_limit", label=CABLE_TYPE_HEAT_LIMIT_KEY),
            OutputColumnModel(col_index=16, method="get_bend_radius", label=CABLE_TYPE_BEND_RADIUS_KEY),
            OutputColumnModel(col_index=17, method="get_rad_tolerance", label=CABLE_TYPE_RAD_TOLERANCE_KEY),
            OutputColumnModel(col_index=18, method="get_total_length", label=CABLE_TYPE_TOTAL_LENGTH_KEY),
            OutputColumnModel(col_index=19, method="get_reel_length", label=CABLE_TYPE_REEL_LENGTH_KEY),
            OutputColumnModel(col_index=20, method="get_reel_qty", label=CABLE_TYPE_REEL_QTY_KEY),
            OutputColumnModel(col_index=21, method="get_lead_time", label=CABLE_TYPE_LEAD_TIME_KEY),
            OutputColumnModel(col_index=22, method="get_procurement_status", label="procurement status"),
            OutputColumnModel(col_index=23, method="get_project_id", label="project id"),
            OutputColumnModel(col_index=24, method="get_tech_system_id", label="technical system"),
            OutputColumnModel(col_index=25, method="get_owner_user_id", label="owner user"),
            OutputColumnModel(col_index=26, method="get_owner_group_id", label="owner group"),
        ]
        return column_list

    def input_handler_list(self):
        global name_manager
        handler_list = [
            SourceHandler(CABLE_TYPE_MANUFACTURER_KEY, self.source_id_manager, self.api, self.missing_source_list),
        ]
        return handler_list

    def get_output_object(self, input_dict):

        logging.debug("adding output object for: %s" % input_dict[CABLE_TYPE_NAME_KEY])
        return CableTypeOutputObject(helper=self, input_dict=input_dict)

    def close(self):
        if len(self.missing_source_list) > 0:

            if not self.args.infoFile:
                print("provide command line arg 'infoFile' to generate debugging output file")
                return

            output_book = xlsxwriter.Workbook(self.args.infoFile)
            output_sheet = output_book.add_worksheet()

            output_sheet.write(0, 0, "missing sources")

            row_index = 1
            for src in self.missing_source_list:
                output_sheet.write(row_index, 0, src)
                row_index = row_index + 1

            output_book.close()



class CableTypeOutputObject(OutputObject):

    def __init__(self, helper, input_dict):
        super().__init__(helper, input_dict)

    def get_name(self):
        return self.input_dict[CABLE_TYPE_NAME_KEY]

    def get_alt_name(self):
        return None

    def get_description(self):
        return self.input_dict[CABLE_TYPE_DESCRIPTION_KEY]

    def get_link_url(self):
        return self.input_dict[CABLE_TYPE_LINK_URL_KEY]

    def get_image_url(self):
        return self.input_dict[CABLE_TYPE_IMAGE_URL_KEY]

    def get_manufacturer_id(self):
        source_name = self.input_dict[CABLE_TYPE_MANUFACTURER_KEY]
        return self.helper.source_id_manager.get_id_for_name(source_name)

    def get_part_number(self):
        return self.input_dict[CABLE_TYPE_PART_NUMBER_KEY]

    def get_alt_part_number(self):
        return self.input_dict[CABLE_TYPE_ALT_PART_NUMBER_KEY]

    def get_diameter(self):
        return self.input_dict[CABLE_TYPE_DIAMETER_KEY]

    def get_weight(self):
        return self.input_dict[CABLE_TYPE_WEIGHT_KEY]

    def get_conductors(self):
        return self.input_dict[CABLE_TYPE_CONDUCTORS_KEY]

    def get_insulation(self):
        return self.input_dict[CABLE_TYPE_INSULATION_KEY]

    def get_jacket_color(self):
        return self.input_dict[CABLE_TYPE_JACKET_COLOR_KEY]

    def get_voltage_rating(self):
        return self.input_dict[CABLE_TYPE_VOLTAGE_RATING_KEY]

    def get_fire_load(self):
        return self.input_dict[CABLE_TYPE_FIRE_LOAD_KEY]

    def get_heat_limit(self):
        return self.input_dict[CABLE_TYPE_HEAT_LIMIT_KEY]

    def get_bend_radius(self):
        return self.input_dict[CABLE_TYPE_BEND_RADIUS_KEY]

    def get_rad_tolerance(self):
        return self.input_dict[CABLE_TYPE_RAD_TOLERANCE_KEY]

    def get_total_length(self):
        return self.input_dict[CABLE_TYPE_TOTAL_LENGTH_KEY]

    def get_reel_length(self):
        return self.input_dict[CABLE_TYPE_REEL_LENGTH_KEY]

    def get_reel_qty(self):
        return self.input_dict[CABLE_TYPE_REEL_QTY_KEY]

    def get_lead_time(self):
        return self.input_dict[CABLE_TYPE_LEAD_TIME_KEY]

    def get_procurement_status(self):
        if len(self.input_dict[CABLE_TYPE_RECEIVED_KEY]) > 0:
            return "Received"
        elif len(self.input_dict[CABLE_TYPE_ORDERED_KEY]) > 0:
            return "Ordered"
        else:
            return "Unspecified"

    def get_procurement_info(self):
        proc_info = ""
        if len(str(self.input_dict[CABLE_TYPE_ORDERED_KEY])) > 0:
            proc_info = proc_info + "Ordered: " + str(self.input_dict[CABLE_TYPE_ORDERED_KEY]) + ". "
        if len(str(self.input_dict[CABLE_TYPE_RECEIVED_KEY])) > 0:
            proc_info = proc_info + "Received: " + str(self.input_dict[CABLE_TYPE_RECEIVED_KEY]) + "."

        return proc_info

    def get_project_id(self):
        return self.helper.get_args().projectId

    def get_tech_system_id(self):
        return self.helper.get_args().techSystemId

    def get_owner_user_id(self):
        return self.helper.get_args().ownerUserId

    def get_owner_group_id(self):
        return self.helper.get_args().ownerGroupId


@register
class CableInventoryHelper(PreImportHelper):

    def __init__(self):
        super().__init__()
        self.cable_type_id_manager = IdManager()
        self.missing_cable_types = set()

    # Adds helper specific command line args.
    # e.g., "parser.add_argument("--cdbUser", help="CDB User ID for API login", required=True)"
    @staticmethod
    def add_parser_args(parser):
        parser.add_argument("--projectId", help="CDB project ID", required=True)
        parser.add_argument("--ownerUserId", help="CDB owner user ID", required=True)
        parser.add_argument("--ownerGroupId", help="CDB owner group ID", required=True)

    def set_args(self, args):
        super().set_args(args)
        print("CDB project id: %s" % args.projectId)
        print("CDB owner user id: %s" % args.ownerUserId)
        print("CDB owner group id: %s" % args.ownerGroupId)

    @staticmethod
    def tag():
        return "CableInventory"

    def num_input_cols(self):
        return 23

    def input_column_list(self):
        column_list = [
            InputColumnModel(col_index=0, key=CABLE_DESIGN_NAME_KEY, required=True),
            InputColumnModel(col_index=3, key=CABLE_DESIGN_OWNER_KEY, required=True),
            InputColumnModel(col_index=4, key=CABLE_DESIGN_TYPE_KEY, required=True),
            InputColumnModel(col_index=15, key=CABLE_DESIGN_LEGACY_ID_KEY),
            InputColumnModel(col_index=18, key=CABLE_DESIGN_MBA_ID_KEY),
            InputColumnModel(col_index=19, key=CABLE_DESIGN_IMPORT_ID_KEY, required=True),
            InputColumnModel(col_index=20, key=CABLE_DESIGN_QR_ID_KEY, required=False),
        ]
        return column_list

    def output_column_list(self):
        column_list = [
            OutputColumnModel(col_index=0, method="get_cable_type_id", label="Catalog Item"),
            OutputColumnModel(col_index=1, method="get_tag", label="Tag"),
            OutputColumnModel(col_index=2, method="get_qr_id", label="QR ID"),
            OutputColumnModel(col_index=3, method="get_description", label="Description"),
            OutputColumnModel(col_index=4, method="get_status", label="Status"),
            OutputColumnModel(col_index=5, method="get_location", label="Location"),
            OutputColumnModel(col_index=6, method="get_location_details", label="Location_Details"),
            OutputColumnModel(col_index=7, method="get_length", label="Length"),
            OutputColumnModel(col_index=8, method="get_project_id", label="Project ID"),
            OutputColumnModel(col_index=9, method="get_owner_user_id", label="Owner User"),
            OutputColumnModel(col_index=10, method="get_owner_group_id", label="Owner Group"),
        ]
        return column_list

    def input_handler_list(self):
        global name_manager
        handler_list = [
            CableTypeHandler(CABLE_DESIGN_TYPE_KEY, self.cable_type_id_manager, self.api, self.missing_cable_types),
        ]
        return handler_list

    # Treat a row that contains a single non-empty value in the "import id" column as an empty row.
    def input_row_is_empty_custom(self, input_dict, row_num):
        non_empty_count = sum([1 for val in input_dict.values() if len(str(val)) > 0])
        if non_empty_count == 1 and len(str(input_dict[CABLE_DESIGN_IMPORT_ID_KEY])) > 0:
            logging.debug("skipping empty row with non-empty import id: %s row: %d" %
                          (input_dict[CABLE_DESIGN_IMPORT_ID_KEY], row_num))
            return True

    def get_output_object(self, input_dict):

        logging.debug("adding output object for: %s" % input_dict[CABLE_INVENTORY_NAME_KEY])
        return CableInventoryOutputObject(helper=self, input_dict=input_dict)


class CableInventoryOutputObject(OutputObject):

    def __init__(self, helper, input_dict):
        super().__init__(helper, input_dict)

    def get_cable_type_id(self):
        cable_type_name = self.input_dict[CABLE_DESIGN_TYPE_KEY]
        return self.helper.cable_type_id_manager.get_id_for_name(cable_type_name)

    def get_tag(self):
        return "auto"

    def get_qr_id(self):
        return self.input_dict[CABLE_DESIGN_QR_ID_KEY]

    def get_description(self):
        return None

    def get_status(self):
        return "Planned"

    def get_location(self):
        return None

    def get_location_details(self):
        return None

    def get_length(self):
        return None

    def get_project_id(self):
        return self.helper.get_args().projectId

    def get_owner_user_id(self):
        return self.helper.get_args().ownerUserId

    def get_owner_group_id(self):
        return self.helper.get_args().ownerGroupId


@register
class CableDesignHelper(PreImportHelper):

    def __init__(self):
        super().__init__()
        self.rack_manager = RackManager()
        self.md_root = None
        self.cable_type_id_manager = IdManager()
        self.missing_endpoints = set()
        self.missing_cable_types = set()
        self.nonunique_endpoints = set()

    # Adds helper specific command line args.
    # e.g., "parser.add_argument("--cdbUser", help="CDB User ID for API login", required=True)"
    @staticmethod
    def add_parser_args(parser):
        parser.add_argument("--techSystemId", help="CDB technical system ID for owner", required=True)
        parser.add_argument("--projectId", help="CDB project ID", required=True)
        parser.add_argument("--mdRoot", help="CDB top-level parent machine design node name", required=True)
        parser.add_argument("--ownerUserId", help="CDB owner user ID", required=True)
        parser.add_argument("--ownerGroupId", help="CDB owner group ID", required=True)

    def set_args(self, args):
        super().set_args(args)
        print("CDB technical system id: %s" % args.techSystemId)
        print("CDB project id: %s" % args.projectId)
        print("CDB owner user id: %s" % args.ownerUserId)
        print("CDB owner group id: %s" % args.ownerGroupId)
        print("top-level parent machine design node name: %s" % args.mdRoot)
        self.md_root = args.mdRoot
        self.info_file = args.infoFile

    @staticmethod
    def tag():
        return "CableDesign"

    def num_input_cols(self):
        return 23

    def input_column_list(self):
        column_list = [
            InputColumnModel(col_index=0, key=CABLE_DESIGN_NAME_KEY, required=True),
            InputColumnModel(col_index=1, key=CABLE_DESIGN_LAYING_KEY, required=True),
            InputColumnModel(col_index=2, key=CABLE_DESIGN_VOLTAGE_KEY, required=True),
            InputColumnModel(col_index=3, key=CABLE_DESIGN_OWNER_KEY, required=True),
            InputColumnModel(col_index=4, key=CABLE_DESIGN_TYPE_KEY, required=True),
            InputColumnModel(col_index=5, key=CABLE_DESIGN_SRC_LOCATION_KEY, required=True),
            InputColumnModel(col_index=6, key=CABLE_DESIGN_SRC_ANS_KEY, required=True),
            InputColumnModel(col_index=7, key=CABLE_DESIGN_SRC_ETPM_KEY, required=True),
            InputColumnModel(col_index=8, key=CABLE_DESIGN_SRC_ADDRESS_KEY, required=True),
            InputColumnModel(col_index=9, key=CABLE_DESIGN_SRC_DESCRIPTION_KEY, required=True),
            InputColumnModel(col_index=10, key=CABLE_DESIGN_DEST_LOCATION_KEY, required=True),
            InputColumnModel(col_index=11, key=CABLE_DESIGN_DEST_ANS_KEY, required=True),
            InputColumnModel(col_index=12, key=CABLE_DESIGN_DEST_ETPM_KEY, required=True),
            InputColumnModel(col_index=13, key=CABLE_DESIGN_DEST_ADDRESS_KEY, required=True),
            InputColumnModel(col_index=14, key=CABLE_DESIGN_DEST_DESCRIPTION_KEY, required=True),
            InputColumnModel(col_index=15, key=CABLE_DESIGN_LEGACY_ID_KEY),
            InputColumnModel(col_index=16, key=CABLE_DESIGN_FROM_DEVICE_NAME_KEY, required=True),
            InputColumnModel(col_index=17, key=CABLE_DESIGN_TO_DEVICE_NAME_KEY, required=True),
            InputColumnModel(col_index=18, key=CABLE_DESIGN_MBA_ID_KEY),
            InputColumnModel(col_index=19, key=CABLE_DESIGN_IMPORT_ID_KEY, required=True),
            InputColumnModel(col_index=20, key=CABLE_DESIGN_QR_ID_KEY, required=False),
            InputColumnModel(col_index=21, key=CABLE_DESIGN_VIA_ROUTE_KEY, required=False),
            InputColumnModel(col_index=22, key=CABLE_DESIGN_WAYPOINT_ROUTE_KEY, required=False),
        ]
        return column_list

    def output_column_list(self):
        column_list = [
            OutputColumnModel(col_index=0, method="get_name", label="name"),
            OutputColumnModel(col_index=1, method="get_alt_name", label="alt name"),
            OutputColumnModel(col_index=2, method="get_ext_name", label="ext cable name"),
            OutputColumnModel(col_index=3, method="get_import_id", label="import cable id"),
            OutputColumnModel(col_index=4, method="get_alt_id", label="alt cable id"),
            OutputColumnModel(col_index=5, method="get_qr_id", label="legacy qr id"),
            OutputColumnModel(col_index=6, method="get_description", label="description"),
            OutputColumnModel(col_index=7, method="get_laying", label="laying"),
            OutputColumnModel(col_index=8, method="get_voltage", label="voltage"),
            OutputColumnModel(col_index=9, method="get_cable_type_id", label="cable type id"),
            OutputColumnModel(col_index=10, method="get_endpoint1_id", label="endpoint1 id"),
            OutputColumnModel(col_index=11, method="get_endpoint1_description", label="endpoint1 description"),
            OutputColumnModel(col_index=12, method="get_endpoint1_route", label="endpoint1 route"),
            OutputColumnModel(col_index=13, method="get_endpoint2_id", label="endpoint2 id"),
            OutputColumnModel(col_index=14, method="get_endpoint2_description", label="endpoint2 description"),
            OutputColumnModel(col_index=15, method="get_endpoint2_route", label="endpoint2 route"),
            OutputColumnModel(col_index=16, method="get_project_id", label="project id"),
            OutputColumnModel(col_index=17, method="get_tech_system_id", label="technical system"),
            OutputColumnModel(col_index=18, method="get_owner_user_id", label="owner user"),
            OutputColumnModel(col_index=19, method="get_owner_group_id", label="owner group"),
        ]
        return column_list
    
    def input_handler_list(self):
        global name_manager
        handler_list = [
            NamedRangeHandler(CABLE_DESIGN_LAYING_KEY, "_Laying"),
            NamedRangeHandler(CABLE_DESIGN_VOLTAGE_KEY, "_Voltage"),
            NamedRangeHandler(CABLE_DESIGN_OWNER_KEY, "_Owner"),
            ConnectedMenuHandler(CABLE_DESIGN_TYPE_KEY, CABLE_DESIGN_OWNER_KEY),
            CableTypeHandler(CABLE_DESIGN_TYPE_KEY, self.cable_type_id_manager, self.api, self.missing_cable_types),
            NamedRangeHandler(CABLE_DESIGN_SRC_LOCATION_KEY, "_Location"),
            ConnectedMenuHandler(CABLE_DESIGN_SRC_ANS_KEY, CABLE_DESIGN_SRC_LOCATION_KEY),
            ConnectedMenuHandler(CABLE_DESIGN_SRC_ETPM_KEY, CABLE_DESIGN_SRC_ANS_KEY),
            DeviceAddressHandler(CABLE_DESIGN_SRC_ADDRESS_KEY, CABLE_DESIGN_SRC_LOCATION_KEY, CABLE_DESIGN_SRC_ETPM_KEY),
            NamedRangeHandler(CABLE_DESIGN_DEST_LOCATION_KEY, "_Location"),
            ConnectedMenuHandler(CABLE_DESIGN_DEST_ANS_KEY, CABLE_DESIGN_DEST_LOCATION_KEY),
            ConnectedMenuHandler(CABLE_DESIGN_DEST_ETPM_KEY, CABLE_DESIGN_DEST_ANS_KEY),
            DeviceAddressHandler(CABLE_DESIGN_DEST_ADDRESS_KEY, CABLE_DESIGN_DEST_LOCATION_KEY, CABLE_DESIGN_DEST_ETPM_KEY),
            EndpointHandler(CABLE_DESIGN_FROM_DEVICE_NAME_KEY, CABLE_DESIGN_SRC_ETPM_KEY, self.get_md_root(), self.api, self.rack_manager, self.missing_endpoints, self.nonunique_endpoints),
            EndpointHandler(CABLE_DESIGN_TO_DEVICE_NAME_KEY, CABLE_DESIGN_DEST_ETPM_KEY, self.get_md_root(), self.api, self.rack_manager, self.missing_endpoints, self.nonunique_endpoints),
        ]
        return handler_list


    def get_md_root(self):
        return self.md_root

    # Treat a row that contains a single non-empty value in the "import id" column as an empty row.
    def input_row_is_empty_custom(self, input_dict, row_num):
        non_empty_count = sum([1 for val in input_dict.values() if len(str(val)) > 0])
        if non_empty_count == 1 and len(str(input_dict[CABLE_DESIGN_IMPORT_ID_KEY])) > 0:
            logging.debug("skipping empty row with non-empty import id: %s row: %d" %
                          (input_dict[CABLE_DESIGN_IMPORT_ID_KEY], row_num))
            return True

    def get_output_object(self, input_dict):

        logging.debug("adding output object for: %s" % input_dict[CABLE_DESIGN_NAME_KEY])
        return CableDesignOutputObject(helper=self, input_dict=input_dict)

    def close(self):

        if len(self.missing_cable_types) > 0 or len(self.missing_endpoints) > 0 or len(self.nonunique_endpoints) > 0:

            if not self.args.infoFile:
                print("provide command line arg 'infoFile' to generate debugging output file")
                return

            output_book = xlsxwriter.Workbook(self.args.infoFile)
            output_sheet = output_book.add_worksheet()

            output_sheet.write(0, 0, "missing cable types")
            output_sheet.write(0, 1, "missing endpoints")
            output_sheet.write(0, 2, "non-unique endpoints")

            row_index = 1
            for cable_type_name in self.missing_cable_types:
                output_sheet.write(row_index, 0, cable_type_name)
                row_index = row_index + 1

            row_index = 1
            for endpoint_name in self.missing_endpoints:
                output_sheet.write(row_index, 1, endpoint_name)
                row_index = row_index + 1

            row_index = 1
            for endpoint_name in self.nonunique_endpoints:
                output_sheet.write(row_index, 2, endpoint_name)
                row_index = row_index + 1

            output_book.close()


class CableDesignOutputObject(OutputObject):

    def __init__(self, helper, input_dict):
        super().__init__(helper, input_dict)

    @classmethod
    def get_name_cls(cls, row_dict):

        # use legacy_id if specified
        legacy_id = row_dict[CABLE_DESIGN_LEGACY_ID_KEY]
        if len(legacy_id) > 0:
            return legacy_id

        # next try MBA cable id
        mba_id = row_dict[CABLE_DESIGN_MBA_ID_KEY]
        if len(mba_id) > 0:
            return mba_id

        # otherwise use import_id prefixed with "CA "
        return "CA " + cls.get_import_id_cls(row_dict)

    @classmethod
    def get_import_id_cls(cls, row_dict):
        return str(int(row_dict[CABLE_DESIGN_IMPORT_ID_KEY]))

    def get_name(self):
        return self.get_name_cls(self.input_dict)

    def get_alt_name(self):
        return "<" + self.input_dict[CABLE_DESIGN_SRC_ETPM_KEY] + "><" + \
               self.input_dict[CABLE_DESIGN_DEST_ETPM_KEY] + ">:" + \
               self.get_name()

    def get_ext_name(self):
        return self.input_dict[CABLE_DESIGN_NAME_KEY]

    def get_import_id(self):
        return self.get_import_id_cls(self.input_dict);

    def get_alt_id(self):
        return self.input_dict[CABLE_DESIGN_LEGACY_ID_KEY]

    def get_qr_id(self):
        return self.input_dict[CABLE_DESIGN_QR_ID_KEY]

    def get_description(self):
        return ""

    def get_laying(self):
        return self.input_dict[CABLE_DESIGN_LAYING_KEY]

    def get_voltage(self):
        return self.input_dict[CABLE_DESIGN_VOLTAGE_KEY]

    def get_cable_type_id(self):
        cable_type_name = self.input_dict[CABLE_DESIGN_TYPE_KEY]
        return self.helper.cable_type_id_manager.get_id_for_name(cable_type_name)

    def get_endpoint_id(self, input_column_key, container_key):
        endpoint_name = self.input_dict[input_column_key]
        container_name = self.input_dict[container_key]
        return self.helper.rack_manager.get_endpoint_id_for_rack(container_name, endpoint_name)

    def get_endpoint1_id(self):
        return self.get_endpoint_id(CABLE_DESIGN_FROM_DEVICE_NAME_KEY, CABLE_DESIGN_SRC_ETPM_KEY)

    def get_endpoint1_description(self):
        return str(self.input_dict[CABLE_DESIGN_SRC_LOCATION_KEY]) + ":" + \
               str(self.input_dict[CABLE_DESIGN_SRC_ANS_KEY]) + ":" + \
               str(self.input_dict[CABLE_DESIGN_SRC_ETPM_KEY]) + ":" + \
               str(self.input_dict[CABLE_DESIGN_SRC_ADDRESS_KEY]) + ":" + \
               str(self.input_dict[CABLE_DESIGN_SRC_DESCRIPTION_KEY])

    def get_endpoint1_route(self):
        return str(self.input_dict[CABLE_DESIGN_VIA_ROUTE_KEY])

    def get_endpoint2_id(self):
        return self.get_endpoint_id(CABLE_DESIGN_TO_DEVICE_NAME_KEY, CABLE_DESIGN_DEST_ETPM_KEY)

    def get_endpoint2_description(self):
        return str(self.input_dict[CABLE_DESIGN_DEST_LOCATION_KEY]) + ":" + \
               str(self.input_dict[CABLE_DESIGN_DEST_ANS_KEY]) + ":" + \
               str(self.input_dict[CABLE_DESIGN_DEST_ETPM_KEY]) + ":" + \
               str(self.input_dict[CABLE_DESIGN_DEST_ADDRESS_KEY]) + ":" + \
               str(self.input_dict[CABLE_DESIGN_DEST_DESCRIPTION_KEY])

    def get_endpoint2_route(self):
        return str(self.input_dict[CABLE_DESIGN_WAYPOINT_ROUTE_KEY])

    def get_project_id(self):
        return self.helper.get_args().projectId

    def get_tech_system_id(self):
        return self.helper.get_args().techSystemId

    def get_owner_user_id(self):
        return self.helper.get_args().ownerUserId

    def get_owner_group_id(self):
        return self.helper.get_args().ownerGroupId


range_parts = re.compile(r'(\$?)([A-Z]{1,3})(\$?)(\d+)')
def xl_cell_to_rowcol(cell_str):
    """
    NOTE: I borrowed this from the xlsxwriter library since it was useful for parsing Excel $G$1 notation.
    Convert a cell reference in A1 notation to a zero indexed row and column.

    Args:
       cell_str:  A1 style string.

    Returns:
        row, col: Zero indexed cell row and column indices.

    """
    if not cell_str:
        return 0, 0

    match = range_parts.match(cell_str)
    col_str = match.group(2)
    row_str = match.group(4)

    # Convert base26 column string to number.
    expn = 0
    col = 0
    for char in reversed(col_str):
        col += (ord(char) - ord('A') + 1) * (26 ** expn)
        expn += 1

    # Convert 1-index to zero-index
    row = int(row_str) - 1
    col -= 1

    return row, col


def write_validation_report(validation_map, validation_report_file):
    output_book = xlsxwriter.Workbook(validation_report_file)
    output_sheet = output_book.add_worksheet()

    output_sheet.write(0, 0, "input row number")
    output_sheet.write(0, 1, "validation messages")

    row_index = 1
    for key in validation_map:
        output_sheet.write(row_index, 0, key)
        cell_value = ""
        for message in validation_map[key]:
            cell_value = cell_value + message + "\n"
        output_sheet.write(row_index, 1, cell_value)
        row_index = row_index + 1

    output_book.close()


def main():

    global name_manager

    # find --type command line parameter so we can create the appropriate helper subclass before proceeding
    type_arg = "--type"
    if type_arg not in sys.argv:
        sys.exit("--type command line parameter not specified, exiting")
    else:
        type_index = sys.argv.index(type_arg)
        if type_index < (len(sys.argv) - 1):
            type_arg_value = sys.argv[type_index+1]
        else:
            sys.exit("no value specified for --type command line parameter, exiting")

    # create instance of appropriate helper subclass
    if not PreImportHelper.is_valid_type(type_arg_value):
        sys.exit("unknown value for type parameter, expected Source, CableType, or CableDesign, got: %s" % type_arg_value)
    helper = PreImportHelper.create_helper(type_arg_value)

    # process other command line arguments
    parser = argparse.ArgumentParser()
    parser.add_argument("inputFile", help="Data collection workbook xlsx file with 'cable specs' tab")
    parser.add_argument("outputFile", help="Official CDB Sources import format xlsx file")
    parser.add_argument(type_arg, help="Type of pre-import processing (Source, CableType, or CableDesign)", required=True)
    parser.add_argument("--logFile", help="File for log output", required=True)
    parser.add_argument("--validationFile", help="File for validation output", required=True)
    parser.add_argument("--infoFile", help="domain specific xlsx file for reviewing helper actions", required=False)
    parser.add_argument("--cdbUrl", help="CDB system URL", required=True)
    parser.add_argument("--cdbUser", help="CDB User ID for API login", required=True)
    parser.add_argument("--cdbPassword", help="CDB User password for API login", required=True)
    parser.add_argument("--sheetNumber", help="Worksheet number within workbook (1-based)", required=True)
    parser.add_argument("--headerRow", help="Input spreadsheet row number of header row (1-based)", required=True)
    parser.add_argument("--firstDataRow", help="Input spreadsheet row number of first data row (1-based)", required=True)
    parser.add_argument("--lastDataRow", help="Input spreadsheet row number of last data row (1-based)", required=True)
    helper.add_parser_args(parser)
    args = parser.parse_args()
    print("using inputFile: %s" % args.inputFile)
    print("using outputFile: %s" % args.outputFile)
    print("using logFile: %s" % args.logFile)
    print("using validation file: %s" % args.validationFile)
    print("debugging info xlsx file: %s" % args.infoFile)
    print("pre-import type: %s" % args.type)
    print("cdb url: %s" % args.cdbUrl)
    print("cdb user id: %s" % args.cdbUser)
    print("cdb user password: %s" % args.cdbPassword)
    print("worksheet number: %s" % args.sheetNumber)
    print("header row number: %s" % args.headerRow)
    print("first data row number: %s" % args.firstDataRow)
    print("last data row number: %s" % args.lastDataRow)
    helper.set_args(args)

    sheetNum = int(args.sheetNumber)
    headerRowNum = int(args.headerRow)
    firstDataRowNum = int(args.firstDataRow)
    lastDataRowNum = int(args.lastDataRow)

    sheetIndex = sheetNum - 1
    headerIndex = headerRowNum - 1
    firstDataIndex = firstDataRowNum - 1
    lastDataIndex = lastDataRowNum - 1

    # initialize input and output columns
    helper.initialize_input_columns()
    helper.initialize_output_columns()

    # configure logging
    logging.basicConfig(filename=args.logFile, filemode='w', level=logging.DEBUG, format='%(levelname)s - %(message)s')

    # open connection to CDB
    api = CdbApiFactory(args.cdbUrl)
    try:
        api.authenticateUser(args.cdbUser, args.cdbPassword)
        api.testAuthenticated()
    except ApiException:
        sys.exit("CDB login failed")
    helper.set_api(api)

    # open input spreadsheet
    input_book = xlrd.open_workbook(args.inputFile)

    name_manager = ConnectedMenuManager(input_book)

    input_sheet = input_book.sheet_by_index(int(sheetIndex))
    logging.info("input spreadsheet dimensions: %d x %d" % (input_sheet.nrows, input_sheet.ncols))

    # validate input spreadsheet dimensions
    if input_sheet.nrows < lastDataRowNum:
        sys.exit("fewer rows in inputFile: %s than last data row: %d" % (args.inputFile, lastDataRowNum))
    if input_sheet.ncols != helper.num_input_cols():
        sys.exit("inputFile %s doesn't contain expected number of columns: %d" % (args.inputFile, helper.num_input_cols()))

    # process rows from input spreadsheet
    input_valid = True
    output_objects = []
    validation_map = {}
    num_input_rows = 0
    for row_ind in range(firstDataIndex, lastDataIndex + 1):

        current_row_num = row_ind + 1
        num_input_rows = num_input_rows + 1

        logging.debug("processing row %d from input spreadsheet" % current_row_num)

        input_dict = {}

        for col_ind in range(helper.num_input_cols()):
            if col_ind in helper.input_columns:
                # read cell value from spreadsheet
                val = input_sheet.cell(row_ind, col_ind).value
                logging.debug("col: %d value: %s" % (col_ind, str(val)))
                helper.handle_input_cell_value(input_dict=input_dict, index=col_ind, value=val, row_num=current_row_num)

        # ignore row if blank
        if helper.input_row_is_empty(input_dict=input_dict, row_num=current_row_num):
            continue

        row_is_valid = True
        row_valid_messages = []

        # validate row
        (is_valid, valid_messages) = helper.input_row_is_valid(input_dict=input_dict, row_num=row_ind)
        if not is_valid:
            row_is_valid = False
            row_valid_messages.extend(valid_messages)

        # invoke handlers
        (handler_is_valid, handler_messages) = helper.invoke_row_handlers(input_dict=input_dict, row_num=row_ind)
        if not handler_is_valid:
            row_is_valid = False
            row_valid_messages.extend(handler_messages)

        if row_is_valid:
            output_obj = helper.get_output_object(input_dict=input_dict)
            if output_obj:
                output_objects.append(output_obj)
        else:
            input_valid = False
            logging.error("validation errors found for row %d" % current_row_num)
            validation_map[current_row_num] = row_valid_messages

    # create output spreadsheet
    output_book = xlsxwriter.Workbook(args.outputFile)
    output_sheet = output_book.add_worksheet()

    # write output spreadsheet header row
    row_ind = 0
    for col_ind in range(helper.num_output_cols()):
        if col_ind in helper.output_columns:
            label = helper.get_output_column_label(col_index=col_ind)
            output_sheet.write(row_ind, col_ind, label)

    # process output spreadsheet rows
    num_output_rows = 0
    if len(output_objects) == 0:
        logging.info("no output objects, output spreadsheet will be empty")
    for output_obj in output_objects:

        row_ind = row_ind + 1
        num_output_rows = num_output_rows + 1
        current_row_num = row_ind + 1

        logging.debug("processing row %d in output spreadsheet" % current_row_num)

        for col_ind in range(helper.num_output_cols()):
            if col_ind in helper.output_columns:

                val = helper.get_output_cell_value(obj=output_obj, index=col_ind)
                output_sheet.write(row_ind, col_ind, val)

    # save output spreadsheet
    if input_valid:
        output_book.close()

    # clean up helper
    helper.close()

    # close CDB connection
    try:
        api.logOutUser()
    except ApiException:
        sys.exit("CDB logout failed")

    # print validation report
    for key in validation_map:
        print("row: %d" % key)
        for message in validation_map[key]:
            print("\t%s" % message)
    write_validation_report(validation_map, args.validationFile)

    # print summary
    if input_valid:
        summary_msg = "SUMMARY: processed %d input rows and wrote %d output rows" % (num_input_rows, num_output_rows)
    else:
        summary_msg = "ERROR: processed %d input rows but no output spreadsheet generated, see log for errors" % num_input_rows
    print()
    print(summary_msg)
    logging.info(summary_msg)


if __name__ == '__main__':
    main()