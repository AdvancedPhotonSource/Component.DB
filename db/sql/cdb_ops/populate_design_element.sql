LOCK TABLES `design_element` WRITE;
/*!40000 ALTER TABLE `design_element` DISABLE KEYS */;
INSERT INTO `design_element` VALUES
(1,'35-ID-A',1,NULL,2,NULL,NULL,NULL,1.00,6),
(2,'35-ID-D',1,NULL,5,NULL,NULL,NULL,4.00,5),
(3,'35-ID-C',1,NULL,4,NULL,NULL,NULL,3.00,2),
(4,'35-ID-E',1,NULL,6,NULL,NULL,NULL,5.00,3),
(5,'35-ID-B',1,NULL,3,NULL,NULL,NULL,2.00,4),
(6,'Front End Components',2,NULL,NULL,1,402,'Collimator, Ratchet wall plate, FEV, Vac. gauges, Mask',1.00,15),
(7,'Labyrinths',2,NULL,NULL,2,402,'(7) {plus (2) utility access labyrinths} on roof of 35-ID-A',2.00,18),
(8,'Survey Port',2,NULL,NULL,3,402,' (2) - Upstream and downstream walls of 35-ID-A',3.00,21),
(9,'Mask 1',2,NULL,NULL,32,402,'Mask 1, located downstream of front end components',4.00,24),
(10,'Photon Shutter',2,NULL,NULL,36,402,'Photon shutter',7.00,36),
(11,'Mask 2/3',2,NULL,NULL,8,402,'Mask 3 rigidly mounted to Mask 2',8.00,37),
(12,'Shielded Beam Transport',2,NULL,NULL,11,402,'One (1) section from A to B stations',11.00,38),
(13,'White Beam Stop ',2,NULL,NULL,35,402,'White Beam Stop downstream of Collimator 1 and Mono',6.00,34),
(14,'Secondary Bremsstrahlung ',2,NULL,NULL,10,402,'Secondary Bremsstrahlung Shielding. Lead bricks stacked beneath plexiglass shield',10.00,35),
(15,'Safety Shutter',2,NULL,NULL,39,402,'Safery shutter',9.00,39),
(16,'Collimator',2,NULL,NULL,34,402,'Collimator 1 located downstream of Mask 1',5.00,42),
(17,'Guillotines',2,NULL,NULL,12,402,'(2) layers - on downstream wall of 35-ID-A',12.00,43),
(19,'Labyrinths',3,NULL,NULL,2,362,'(10) {plus (1) utility access labyrinth} on roof of 35-ID-B and (1) wall labyrinth on the outboard wall',2.00,47),
(20,'White Beam Stop',3,NULL,NULL,14,362,'Water cooled, mounted to the movable lead stop',3.00,46),
(21,'Guillotines',3,NULL,NULL,12,362,'(2) layers - on up stream wall of 35-ID-B',1.00,50),
(22,'Pink Beam Stop',3,NULL,NULL,16,362,'Pink beam stop mounted to movable lead stop',4.00,49),
(23,'Bremmstrahlung Stop',3,NULL,NULL,15,362,'Movable lead stop and Bremsstrahlung stop',5.00,53),
(24,'Collimator',4,NULL,NULL,24,363,'Collimator downstream of Mask 4 inside tank',4.00,67),
(25,'White Beam Stop',4,NULL,NULL,14,363,'Water cooled mounted to the movable lead stop',6.00,65),
(26,'Access Shield',4,NULL,NULL,22,363,'B to C Station Beam Transport Line Port Access Shield',1.00,66),
(27,'Bremsstrahlung Stop',4,NULL,NULL,15,363,'Movable lead Bremmstrahlung stop',8.00,62),
(28,'Mask 4',4,NULL,NULL,23,363,'Mask downstream of Mask 5',3.00,68),
(29,'Mask 5',4,NULL,NULL,21,363,'Copper and graphite mask, water cooled',2.00,61),
(30,'Pink Beam Stop',4,NULL,NULL,16,363,'Pink beam stop mounted to lead Bremsstrahlung stop',7.00,64),
(31,'Labyrinths',4,NULL,NULL,2,363,'(10) {plus (1) utility access labyrinth on roof} (1) on inboard wall of 35-ID-C',5.00,63),
(36,'White Beam Stop',5,NULL,NULL,14,NULL,'Water cooled, mounted on the movable lead stop',3.00,77),
(37,'Shielded Beam Transport',5,NULL,NULL,11,NULL,'Shielded Beam Transport from 35-ID-C to 35-ID-D',1.00,76),
(38,'Pink Beam Stop',5,NULL,NULL,16,NULL,'Pink beam stop mounted to movable lead stop',4.00,78),
(39,'Bremmstrahlung Stop',5,NULL,NULL,15,NULL,'Movable lead stop and Bremsstrahlung stop',5.00,75),
(40,'Labyrinths',5,NULL,NULL,2,NULL,'(8) {plus (1) utility access labyrinth} on roof, (2) on inboard wall of 35-ID-D',2.00,74),
(41,'Bremsstrahlung Stop',6,NULL,NULL,28,NULL,'Fixed Lead Stop on stand',5.00,80),
(42,'Pink Beam Stop',6,NULL,NULL,16,NULL,'Pink beam stop mounted to lead Bremsstrahlung stop',4.00,82),
(43,'Access Shield',6,NULL,NULL,22,NULL,'D to E Station Beam Transport Line Port Access Shield',1.00,81),
(44,'White Beam Stop',6,NULL,NULL,14,NULL,'Water cooled white beam stop on stand',3.00,79),
(45,'Labyrinths',6,NULL,NULL,2,NULL,'(12) {plus (1) utility access labyrinth} on roof, (1) on upstream wall of 35-ID-E',2.00,83),
(67,'Mask 3',10,NULL,NULL,38,NULL,NULL,NULL,114),
(68,'Mask 2',10,NULL,NULL,37,NULL,NULL,NULL,113),
(69,'Top Level Assembly',11,NULL,NULL,NULL,NULL,NULL,NULL,121),
(70,'A4 Guide Assembly',12,NULL,NULL,42,NULL,NULL,NULL,133),
(72,'X-Ray Source',13,NULL,NULL,NULL,NULL,'',NULL,138),
(74,'Front End',13,NULL,14,NULL,NULL,NULL,NULL,140),
(75,'Beamline',13,NULL,NULL,NULL,NULL,NULL,NULL,142);
/*!40000 ALTER TABLE `design_element` ENABLE KEYS */;
UNLOCK TABLES;
