LOCK TABLES `setting_type` WRITE;
/*!40000 ALTER TABLE `setting_type` DISABLE KEYS */;
INSERT INTO `setting_type` VALUES
(1,'AllowedPropertyValue.List.Display.Description','Display allowed property value description.','false'),
(2,'AllowedPropertyValue.List.Display.Id','Display allowed property value id.','false'),
(3,'AllowedPropertyValue.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(4,'AllowedPropertyValue.List.Display.SortOrder','Display allowed property value sort order.','true'),
(5,'AllowedPropertyValue.List.Display.Units','Display allowed property value units.','true'),
(6,'AllowedPropertyValue.List.FilterBy.Description','Filter for allowed property value description.',NULL),
(7,'AllowedPropertyValue.List.FilterBy.SortOrder','Filter for allowed property value class sort order.',NULL),
(8,'AllowedPropertyValue.List.FilterBy.Units','Filter for allowed property value units.',NULL),
(9,'AllowedPropertyValue.List.FilterBy.Value','Filter for allowed property value.',NULL),
(10,'Component.List.Display.CreatedByUser','Display created by username.','false'),
(11,'Component.List.Display.CreatedOnDateTime','Display created on date/time.','false'),
(12,'Component.List.Display.Description','Display component description.','true'),
(13,'Component.List.Display.Id','Display component id.','false'),
(14,'Component.List.Display.LastModifiedByUser','Display last modified by username.','false'),
(15,'Component.List.Display.LastModifiedOnDateTime','Display last modified on date/time.','false'),
(16,'Component.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(17,'Component.List.Display.OwnerUser','Display owner username.','true'),
(18,'Component.List.Display.OwnerGroup','Display owner group name.','true'),
(19,'Component.List.Display.PropertyTypeId1','Display property value for property type id #1.','1'),
(20,'Component.List.Display.PropertyTypeId2','Display property value for property type id #2.',''),
(21,'Component.List.Display.PropertyTypeId3','Display property value for property type id #3.',''),
(22,'Component.List.Display.PropertyTypeId4','Display property value for property type id #4.',''),
(23,'Component.List.Display.PropertyTypeId5','Display property value for property type id #5.',''),
(24,'Component.List.Display.Type','Display component type.','true'),
(25,'Component.List.Display.Category','Display component type category.','true'),
(26,'Component.List.FilterBy.CreatedByUser','Filter for components that were created by username.',NULL),
(27,'Component.List.FilterBy.CreatedOnDateTime','Filter for components that were created on date/time.',NULL),
(28,'Component.List.FilterBy.Description','Filter for components by description.',NULL),
(29,'Component.List.FilterBy.LastModifiedByUser','Filter for components that were last modified by username.',NULL),
(30,'Component.List.FilterBy.LastModifiedOnDateTime','Filter for components that were last modified on date/time.',NULL),
(31,'Component.List.FilterBy.PropertyValue1','Filter for property value with displayed property type id #1.',''),
(32,'Component.List.FilterBy.PropertyValue2','Filter for property value with displayed property type id #2.',''),
(33,'Component.List.FilterBy.PropertyValue3','Filter for property value with displayed property type id #3.',''),
(34,'Component.List.FilterBy.PropertyValue4','Filter for property value with displayed property type id #4.',''),
(35,'Component.List.FilterBy.PropertyValue5','Filter for property value with displayed property type id #5.',''),
(36,'Component.List.FilterBy.Name','Filter for components by name.',NULL),
(37,'Component.List.FilterBy.OwnerUser','Filter for components by owner username.',NULL),
(38,'Component.List.FilterBy.OwnerGroup','Filter for components by owner group name.',NULL),
(39,'Component.List.FilterBy.Type','Filter for components by type.',NULL),
(40,'Component.List.FilterBy.Category','Filter for components by type category.',NULL),
(41,'ComponentInstance.List.Display.CreatedByUser','Display created by username.','false'),
(42,'ComponentInstance.List.Display.CreatedOnDateTime','Display created on date/time.','false'),
(43,'ComponentInstance.List.Display.Description','Display component instance description.','true'),
(44,'ComponentInstance.List.Display.Id','Display component instance id.','false'),
(45,'ComponentInstance.List.Display.LastModifiedByUser','Display last modified by username.','false'),
(46,'ComponentInstance.List.Display.LastModifiedOnDateTime','Display last modified on date/time.','false'),
(47,'ComponentInstance.List.Display.LocationDetails','Display component instance location details.','false'),
(48,'ComponentInstance.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(49,'ComponentInstance.List.Display.OwnerUser','Display owner username.','true'),
(50,'ComponentInstance.List.Display.OwnerGroup','Display owner group name.','true'),
(51,'ComponentInstance.List.Display.PropertyTypeId1','Display property value for property type id #1.',''),
(52,'ComponentInstance.List.Display.PropertyTypeId2','Display property value for property type id #2.',''),
(53,'ComponentInstance.List.Display.PropertyTypeId3','Display property value for property type id #3.',''),
(54,'ComponentInstance.List.Display.PropertyTypeId4','Display property value for property type id #4.',''),
(55,'ComponentInstance.List.Display.PropertyTypeId5','Display property value for property type id #5.',''),
(56,'ComponentInstance.List.Display.QrId','Display component instance QR id.','true'),
(57,'ComponentInstance.List.Display.SerialNumber','Display component instance serial number.','false'),
(58,'ComponentInstance.List.FilterBy.CreatedByUser','Filter for components that were created by username.',NULL),
(59,'ComponentInstance.List.FilterBy.CreatedOnDateTime','Filter for components that were created on date/time.',NULL),
(60,'ComponentInstance.List.FilterBy.Description','Filter for components by description.',NULL),
(61,'ComponentInstance.List.FilterBy.LastModifiedByUser','Filter for components that were last modified by username.',NULL),
(62,'ComponentInstance.List.FilterBy.LastModifiedOnDateTime','Filter for components that were last modified on date/time.',NULL),
(63,'ComponentInstance.List.FilterBy.Location','Filter for component instance location.',NULL),
(64,'ComponentInstance.List.FilterBy.LocationDetails','Filter for component instance location details.',NULL),
(65,'ComponentInstance.List.FilterBy.OwnerUser','Filter for component instances by owner username.',NULL),
(66,'ComponentInstance.List.FilterBy.OwnerGroup','Filter for component instances by owner group name.',NULL),
(67,'ComponentInstance.List.FilterBy.PropertyValue1','Filter for property value with displayed property type id #1.',''),
(68,'ComponentInstance.List.FilterBy.PropertyValue2','Filter for property value with displayed property type id #2.',''),
(69,'ComponentInstance.List.FilterBy.PropertyValue3','Filter for property value with displayed property type id #3.',''),
(70,'ComponentInstance.List.FilterBy.PropertyValue4','Filter for property value with displayed property type id #4.',''),
(71,'ComponentInstance.List.FilterBy.PropertyValue5','Filter for property value with displayed property type id #5.',''),
(72,'ComponentInstance.List.FilterBy.QrId','Filter for component instance QR id.',NULL),
(73,'ComponentInstance.List.FilterBy.SerialNumber','Filter for component instance serial number.',NULL),
(74,'ComponentInstance.List.FilterBy.Tag','Filter for component instance tag.',NULL),
(75,'ComponentSource.List.Display.Cost','Display component cost.','true'),
(76,'ComponentSource.List.Display.Description','Display component source description.','true'),
(77,'ComponentSource.List.Display.Id','Display component source id.','false'),
(78,'ComponentSource.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(79,'ComponentSource.List.Display.PartNumber','Display component part number.','true'),
(80,'ComponentSource.List.FilterBy.Cost','Filter for component sources by cost.',NULL),
(81,'ComponentSource.List.FilterBy.Description','Filter for component sources by description.',NULL),
(82,'ComponentSource.List.FilterBy.PartNumber','Filter for component sources by part number.',NULL),
(83,'ComponentSource.List.FilterBy.SourceName','Filter for component sources by name.',NULL),
(84,'ComponentType.List.Display.Category','Display component type category.','true'),
(85,'ComponentType.List.Display.Description','Display component type description.','true'),
(86,'ComponentType.List.Display.Id','Display component type id.','false'),
(87,'ComponentType.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(88,'ComponentType.List.FilterBy.Category','Filter for component type category.',NULL),
(89,'ComponentType.List.FilterBy.Description','Filter for component type description.',NULL),
(90,'ComponentType.List.FilterBy.Name','Filter for component type name.',NULL),
(91,'ComponentTypeCategory.List.Display.Description','Display component type category description.','true'),
(92,'ComponentTypeCategory.List.Display.Id','Display component type category id.','false'),
(93,'ComponentTypeCategory.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(94,'ComponentTypeCategory.List.FilterBy.Description','Filter for component type category description.',NULL),
(95,'ComponentTypeCategory.List.FilterBy.Name','Filter for component type category name.',NULL),
(96,'ComponentTypePropertyType.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(97,'ComponentTypePropertyType.List.Display.Id','Display component type property type id.','false'),
(98,'ComponentTypePropertyType.List.FilterBy.PropertyTypeName','Filter for component type property type name.',NULL),
(99,'Design.List.Display.CreatedByUser','Display created by username.','false'),
(100,'Design.List.Display.CreatedOnDateTime','Display created on date/time.','false'),
(101,'Design.List.Display.Description','Display design description.','true'),
(102,'Design.List.Display.Id','Display design id.','false'),
(103,'Design.List.Display.LastModifiedByUser','Display last modified by username.','false'),
(104,'Design.List.Display.LastModifiedOnDateTime','Display last modified on date/time.','false'),
(105,'Design.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(106,'Design.List.Display.OwnerUser','Display owner username.','true'),
(107,'Design.List.Display.OwnerGroup','Display owner group name.','true'),
(108,'Design.List.FilterBy.CreatedByUser','Filter for designs that were created by username.',NULL),
(109,'Design.List.FilterBy.CreatedOnDateTime','Filter for designs that were created on date/time.',NULL),
(110,'Design.List.FilterBy.Description','Filter for designs by description.',NULL),
(111,'Design.List.FilterBy.LastModifiedByUser','Filter for designs that were last modified by username.',NULL),
(112,'Design.List.FilterBy.LastModifiedOnDateTime','Filter for designs that were last modified on date/time.',NULL),
(113,'Design.List.FilterBy.Name','Filter for designs by name.',NULL),
(114,'Design.List.FilterBy.OwnerUser','Filter for designs by owner username.',NULL),
(115,'Design.List.FilterBy.OwnerGroup','Filter for designs by owner group name.',NULL),
(116,'DesignElement.List.Display.ChildDesign','Display child design.','true'),
(117,'DesignElement.List.Display.Component','Display component.','true'),
(118,'DesignElement.List.Display.CreatedByUser','Display created by username.','false'),
(119,'DesignElement.List.Display.CreatedOnDateTime','Display created on date/time.','false'),
(120,'DesignElement.List.Display.Description','Display design element description.','false'),
(121,'DesignElement.List.Display.Id','Display design element id.','false'),
(122,'DesignElement.List.Display.LastModifiedByUser','Display last modified by username.','false'),
(123,'DesignElement.List.Display.LastModifiedOnDateTime','Display last modified on date/time.','false'),
(124,'DesignElement.List.Display.Location','Display location.','true'),
(125,'DesignElement.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(126,'DesignElement.List.Display.OwnerUser','Display owner username.','true'),
(127,'DesignElement.List.Display.OwnerGroup','Display owner group name.','true'),
(128,'DesignElement.List.Display.SortOrder','Display design element sort order.','true'),
(129,'DesignElement.List.FilterBy.ChildDesign','Filter for child design.',NULL),
(130,'DesignElement.List.FilterBy.Component','Filter for component.',NULL),
(131,'DesignElement.List.FilterBy.CreatedByUser','Filter for design elements that were created by username.',NULL),
(132,'DesignElement.List.FilterBy.CreatedOnDateTime','Filter for design elements that were created on date/time.',NULL),
(133,'DesignElement.List.FilterBy.Description','Filter for design elements by description.',NULL),
(134,'DesignElement.List.FilterBy.LastModifiedByUser','Filter for design elements that were last modified by username.',NULL),
(135,'DesignElement.List.FilterBy.LastModifiedOnDateTime','Filter for design elements that were last modified on date/time.',NULL),
(136,'DesignElement.List.FilterBy.Location','Filter for component.',NULL),
(137,'DesignElement.List.FilterBy.Name','Filter for design elements by name.',NULL),
(138,'DesignElement.List.FilterBy.OwnerUser','Filter for design elements by owner username.',NULL),
(139,'DesignElement.List.FilterBy.OwnerGroup','Filter for design elements by owner group name.',NULL),
(140,'DesignElement.List.FilterBy.SortOrder','Filter for design elements by sort order.',NULL),
(141,'DesignLink.List.Display.Description','Display design link description.','false'),
(142,'DesignLink.List.Display.Id','Display design link id.','false'),
(143,'DesignLink.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(144,'DesignLink.List.Display.Tag','Display design link tag.','true'),
(145,'Location.List.Display.Description','Display location description.','true'),
(146,'Location.List.Display.FlatTableView','Display flat table view for location list.','false'),
(147,'Location.List.Display.Id','Display location id.','false'),
(148,'Location.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(149,'Location.List.Display.Parent','Display location parent.','true'),
(150,'Location.List.Display.Type','Display location type.','true'),
(151,'Location.List.FilterBy.Description','Filter for location type description.',NULL),
(152,'Location.List.FilterBy.Name','Filter for location type name.',NULL),
(153,'Location.List.FilterBy.Parent','Filter for location parent.',NULL),
(154,'Location.List.FilterBy.Type','Filter for location type.',NULL),
(155,'LocationType.List.Display.Description','Display location type description.','true'),
(156,'LocationType.List.Display.Id','Display location type id.','false'),
(157,'LocationType.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(158,'LocationType.List.FilterBy.Description','Filter for location type description.',NULL),
(159,'LocationType.List.FilterBy.Name','Filter for location type name.',NULL),
(160,'Log.List.Display.Attachments','Display log entry attachments.','true'),
(161,'Log.List.Display.EnteredOnDateTime','Display log entry entered on date/time.','true'),
(162,'Log.List.Display.EnteredByUser','Display log entry entered by user.','true'),
(163,'Log.List.Display.Id','Display log entry id.','false'),
(164,'Log.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(165,'Log.List.Display.Topic','Display log topic.','false'),
(166,'Log.List.FilterBy.EnteredOnDateTime','Filter for log entry entered on date/time.',NULL),
(167,'Log.List.FilterBy.EnteredByUser','Filter for log entry entered by user.',NULL),
(168,'Log.List.FilterBy.Text','Filter for log entry text.',NULL),
(169,'Log.List.FilterBy.Topic','Filter for log topic.',NULL),
(170,'PropertyType.List.Display.Category','Display property type category.','true'),
(171,'PropertyType.List.Display.DefaultUnits','Display property type default units.','false'),
(172,'PropertyType.List.Display.DefaultValue','Display property type default value.','false'),
(173,'PropertyType.List.Display.Description','Display property type description.','true'),
(174,'PropertyType.List.Display.Handler','Display property type class handler.','true'),
(175,'PropertyType.List.Display.Id','Display property type id.','false'),
(176,'PropertyType.List.Display.IsDynamic','Display dynamic property type designation.','false'),
(177,'PropertyType.List.Display.IsUserWriteable','Display user-writeable property type designation.','false'),
(178,'PropertyType.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(179,'PropertyType.List.FilterBy.Category','Filter for property type category.',NULL),
(180,'PropertyType.List.FilterBy.DefaultUnits','Filter for property type default units.',NULL),
(181,'PropertyType.List.FilterBy.DefaultValue','Filter for property type default value.',NULL),
(182,'PropertyType.List.FilterBy.Description','Filter for property type description.',NULL),
(183,'PropertyType.List.FilterBy.Handler','Filter for property type class handler.',NULL),
(184,'PropertyType.List.FilterBy.IsDynamic','Filter for dynamic property type designation.',NULL),
(185,'PropertyType.List.FilterBy.IsUserWriteable','Filter for user-writeable property type designation.',NULL),
(186,'PropertyType.List.FilterBy.Name','Filter for property type name.',NULL),
(187,'PropertyTypeCategory.List.Display.Description','Display property type category description.','true'),
(188,'PropertyTypeCategory.List.Display.Id','Display property type category id.','false'),
(189,'PropertyTypeCategory.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(190,'PropertyTypeCategory.List.FilterBy.Description','Filter for property type category description.',NULL),
(191,'PropertyTypeCategory.List.FilterBy.Name','Filter for property type category name.',NULL),
(192,'PropertyTypeHandler.List.Display.Description','Display property type handler description.','true'),
(193,'PropertyTypeHandler.List.Display.Id','Display property type handler id.','false'),
(194,'PropertyTypeHandler.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(195,'PropertyTypeHandler.List.FilterBy.Description','Filter for property type handler description.',NULL),
(196,'PropertyTypeHandler.List.FilterBy.Name','Filter for property type handler name.',NULL),
(197,'PropertyValue.List.Display.Description','Display value entry description.','false'),
(198,'PropertyValue.List.Display.EnteredOnDateTime','Display value entry entered on date/time.','false'),
(199,'PropertyValue.List.Display.EnteredByUser','Display value entry entered by user.','false'),
(200,'PropertyValue.List.Display.Id','Display property value entry id.','false'),
(201,'PropertyValue.List.Display.IsDynamic','Display dynamic property type designation.','true'),
(202,'PropertyValue.List.Display.IsUserWriteable','Display user-writeable property type designation.','false'),
(203,'PropertyValue.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(204,'PropertyValue.List.Display.Tag','Display property value tag.','true'),
(205,'PropertyValue.List.Display.TypeCategory','Display property value type category.','false'),
(206,'PropertyValue.List.Display.Units','Display value units.','true'),
(207,'PropertyValue.List.FilterBy.Description','Filter for value description.',NULL),
(208,'PropertyValue.List.FilterBy.EnteredOnDateTime','Filter for value entry entered on date/time.',NULL),
(209,'PropertyValue.List.FilterBy.EnteredByUser','Filter for value entry entered by user.',NULL),
(210,'PropertyValue.List.FilterBy.IsDynamic','Filter for dynamic property value designation.',NULL),
(211,'PropertyValue.List.FilterBy.IsUserWriteable','Filter for user-writeable property value designation.',NULL),
(212,'PropertyValue.List.FilterBy.Tag','Filter for property value tag.',NULL),
(213,'PropertyValue.List.FilterBy.Type','Filter for property value type.',NULL),
(214,'PropertyValue.List.FilterBy.TypeCategory','Filter for property value type category.',NULL),
(215,'PropertyValue.List.FilterBy.Units','Filter for value units.',NULL),
(216,'PropertyValue.List.FilterBy.Value','Filter for value entry.',NULL),
(217,'PropertyValueHistory.List.Display.Description','Display value entry description.','true'),
(218,'PropertyValueHistory.List.Display.EnteredOnDateTime','Display value entry entered on date/time.','true'),
(219,'PropertyValueHistory.List.Display.EnteredByUser','Display value entry entered by user.','true'),
(220,'PropertyValueHistory.List.Display.Id','Display property value entry id.','false'),
(221,'PropertyValueHistory.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(222,'PropertyValueHistory.List.Display.Tag','Display value tag.','true'),
(223,'PropertyValueHistory.List.Display.Units','Display value units.','true'),
(224,'PropertyValueHistory.List.FilterBy.Description','Filter for value description.',NULL),
(225,'PropertyValueHistory.List.FilterBy.EnteredOnDateTime','Filter for value entry entered on date/time.',NULL),
(226,'PropertyValueHistory.List.FilterBy.EnteredByUser','Filter for value entry entered by user.',NULL),
(227,'PropertyValueHistory.List.FilterBy.Tag','Filter for value tag.',NULL),
(228,'PropertyValueHistory.List.FilterBy.Units','Filter for value units.',NULL),
(229,'PropertyValueHistory.List.FilterBy.Value','Filter for value entry.',NULL),
(230,'Search.List.Display.Components','Display search result for components.','true'),
(231,'Search.List.Display.ComponentInstances','Display search result for component instances.','true'),
(232,'Search.List.Display.ComponentTypes','Display search result for component types.','true'),
(233,'Search.List.Display.ComponentTypeCategories','Display search result for component type categories.','true'),
(234,'Search.List.Display.Designs','Display search result for designs.','true'),
(235,'Search.List.Display.DesignElements','Display search result for design elements.','true'),
(236,'Search.List.Display.Locations','Display search result for locations.','true'),
(237,'Search.List.Display.LocationTypes','Display search result for location types.','true'),
(238,'Search.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(239,'Search.List.Display.PropertyTypes','Display search result for property types.','true'),
(240,'Search.List.Display.PropertyTypeCategories','Display search result for property type categories.','true'),
(241,'Search.List.Display.Sources','Display search result for sources.','true'),
(242,'Search.List.Display.Users','Display search result for users.','true'),
(243,'Search.List.Display.UserGroups','Display search result for user groups.','true'),
(244,'Source.List.Display.Description','Display source description.','true'),
(245,'Source.List.Display.Id','Display source id.','false'),
(246,'Source.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(247,'Source.List.FilterBy.Description','Filter for source description.',NULL),
(248,'Source.List.FilterBy.Name','Filter for source name.',NULL),
(249,'UserInfo.List.Display.Description','Display user description.','false'),
(250,'UserInfo.List.Display.Email','Display user email.','true'),
(251,'UserInfo.List.Display.FirstName','Display user first name.','true'),
(252,'UserInfo.List.Display.Id','Display user id.','false'),
(253,'UserInfo.List.Display.Groups','Display user groups.','true'),
(254,'UserInfo.List.Display.LastName','Display user last name.','true'),
(255,'UserInfo.List.Display.MiddleName','Display user middle name.','false'),
(256,'UserInfo.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(257,'UserInfo.List.FilterBy.Description','Filter for user description.',NULL),
(258,'UserInfo.List.FilterBy.Email','Filter for user email.',NULL),
(259,'UserInfo.List.FilterBy.FirstName','Filter for user first name.',NULL),
(260,'UserInfo.List.FilterBy.Groups','Filter for user groups.',NULL),
(261,'UserInfo.List.FilterBy.LastName','Filter for user last name.',NULL),
(262,'UserInfo.List.FilterBy.MiddleName','Filter for user middle name.',NULL),
(263,'UserInfo.List.FilterBy.Username','Filter for username.',NULL),
(264,'UserGroup.List.Display.Description','Display user group description.','true'),
(265,'UserGroup.List.Display.Id','Display user group id.','false'),
(266,'UserGroup.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25'),
(267,'UserGroup.List.FilterBy.Description','Filter for user group description.',NULL),
(268,'UserGroup.List.FilterBy.Name','Filter for user group name.',NULL),
(269,'UserSetting.List.Display.NumberOfItemsPerPage','Display specified number of items per page.','25');
/*!40000 ALTER TABLE `setting_type` ENABLE KEYS */;
UNLOCK TABLES;
