LOCK TABLES `property_type` WRITE;
/*!40000 ALTER TABLE `property_type` DISABLE KEYS */;
INSERT INTO `property_type` VALUES
(1,'EDP Collection','Provides a link to an EDP collection of documents. The handler will accept the full EDP ID string (e.g. EDP_000123) or just the collection number (e.g. 123)',4,4,'',''),
(2,'QA Level','Enumerated values of A | B | C | D',2,NULL,'D',''),
(3,'QA Inspection Template','Inspection Procedure Document',2,NULL,NULL,NULL),
(4,'QA Inspection Report','Inspection Result Document',2,NULL,NULL,NULL),
(5,'Electrical Equipment Status','NRTL Approved or APS Inspection Required or Not Required',2,NULL,NULL,NULL),
(6,'Electrical Inspection #','Inspection # from DEEI (use desc of Status?)',2,NULL,NULL,NULL),
(7,'Documentation (WEB)','Provides a link to any web address (URL)',4,3,'',''),
(8,'Form Factor','',3,NULL,'',''),
(9,'Slot Length','',3,NULL,NULL,NULL),
(10,'Required Water Flow',NULL,3,NULL,NULL,NULL),
(11,'WBS-P6','',1,NULL,NULL,NULL),
(12,'Traveler Template (ICMS)','',2,5,'',''),
(13,'Traveler Instance (ICMS)','',2,5,'',''),
(14,'Image',NULL,4,2,NULL,NULL),
(15,'Document (Upload)','',4,1,'',''),
(16,'Document (ICMS)','',4,5,'',''),
(17,'PDMLink Drawing','Provides a link to PDMLink, enter the drawing number including the extension e.g. U221020202-110001.asm',4,6,'',''),
(18,'AMOS Order','Provides a link to AMOS, enter the Master Order number, e.g. MO_nnnnnn ',4,7,'',''),
(19,'Purchase Requisition','Provides a link to PARIS. Enter the PR number, e.g. Fy-xxxxxx , in the Value field',4,8,'',''),
(21,'QA Inspection Complete','',2,10,'false',NULL),
(22,'Pressure System Component','',2,10,NULL,NULL),
(23,'Custodian','',NULL,NULL,NULL,NULL),
(24,'Date of Manufacture','',5,11,NULL,NULL),
(25,'Date In Service','',5,11,NULL,NULL),
(26,'Date Next Maintenance Due','',5,11,NULL,NULL),
(27,'Maintenance Record Template','',5,5,NULL,NULL),
(28,'Maintenance Record','',5,5,NULL,NULL),
(29,'Fiducialization Record Template','',5,5,NULL,NULL),
(30,'Fiducialization Record','',5,5,NULL,NULL),
(31,'Alignment Record Template','',5,5,NULL,NULL),
(32,'Alignment Record','',5,5,NULL,NULL),
(33,'Design Status','',6,NULL,NULL,NULL),
(34,'Component Instance Status','',6,NULL,NULL,NULL),
(35,'Length','',7,NULL,'',''),
(36,'Current','Could be max current or operating current',3,NULL,'0.0','Amperes'),
(37,'Weight','',3,NULL,'',''),
(38,'Termination','Cable end termination types',3,NULL,'',''),
(39,'sStart','',7,NULL,'','meters'),
(40,'sEnd','',7,NULL,'','meters'),
(41,'X','',7,NULL,'',''),
(42,'Z','',7,NULL,'',''),
(43,'theta','',7,NULL,'',''),
(44,'betax','',7,NULL,'',''),
(45,'betay','',7,NULL,'',''),
(46,'etax','',7,NULL,'',''),
(47,'etaxp','',7,NULL,'',''),
(48,'alphax','',7,NULL,'',''),
(49,'alphay','',7,NULL,'',''),
(50,'psix','',7,NULL,'',''),
(51,'psiy','',7,NULL,'',''),
(52,'rho','',7,NULL,'',''),
(53,'B','',7,NULL,'',''),
(54,'AngleDeg','',7,NULL,'',''),
(55,'B1','',7,NULL,'',''),
(56,'B2','',7,NULL,'',''),
(57,'B1L','',7,NULL,'',''),
(58,'K1','',7,NULL,'',''),
(59,'K2','',7,NULL,'',''),
(60,'B2L','',7,NULL,'',''),
(61,'Magnet Measurement Procedure','',9,3,'',''),
(62,'VertexPointX','',7,NULL,'',''),
(63,'VertexPointZ','',7,NULL,'',''),
(64,'Installed Component QR ID','QR ID of an installed instance of a component',3,NULL,'',''),
(65,'Calibration Record (ICMS)','',5,5,'',''),
(66,'Calibration Date','',5,11,'',''),
(67,'Build Variant','Used to capture unique fabrication for different pieces of the same component',3,NULL,'',''),
(68,'Option / Feature','Used to record an optional feature unique to this component instance.',3,NULL,'',''),
(69,'Network Node Name','',NULL,NULL,'',''),
(70,'Traveler Instance (Upload)','',2,1,'',''),
(71,'Traveler Template (Upload)','',2,1,'',''),
(72,'Component Design','Used to associate a component with a design',8,12,'',''),
(73,'Measurement (plot)','PDF of measurement results',3,1,'',''),
(74,'WBS-DCC','Document Control Center WBS numbers',1,NULL,NULL,NULL),
(75,'Configuration Control','',10,10,'',''),
(76,'Supplemental Shielding','',10,10,'',''),
(77,'Distance From Source Point','',10,NULL,'','meters'),
(78,'Dimension','',3,NULL,'',''),
(79,'Verified','',10,11,'',''),
(80,'Critical Component','',10,10,'',''),
(81,'Traveler Template (Electronic)','Allows integration of traveler templates from traveler application.',2,13,'',''),
(82,'Traveler Instance (Electronic)','Allows integration of traveler instances from traveler application.',2,14,'',''),
(83,'CSI #','Record Argonne\'s CSI # of a component instance',4,NULL,'','');
/*!40000 ALTER TABLE `property_type` ENABLE KEYS */;
UNLOCK TABLES;
