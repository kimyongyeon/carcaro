drop table recommend;
drop table charge_history;
drop table settlement;
drop table pre_settlement;
drop table driver;


delimiter ;
CREATE TABLE `driver` (
  `driverId` varchar(20) NOT NULL,
  `passwd` varchar(20) NOT NULL,
  `name` varchar(20) NOT NULL,
  `phone` varchar(15) NOT NULL,
  `residentNo` varchar(13) NOT NULL,
  `joinDate` timestamp NULL default CURRENT_TIMESTAMP,  
  `authorizationNo` varchar(45) NOT NULL,
  `email` varchar(30) NOT NULL,
  `licenseType` int(11) NOT NULL,
  `licenseAuto` tinyint(1) NOT NULL,
  `career` int(11) NOT NULL,
  `company` varchar(20) NOT NULL,
  `address` varchar(100) NOT NULL,
  `agreeReceive` int(11) DEFAULT '0',
  `advertisement` varchar(20) DEFAULT NULL,
  `level` int(11) DEFAULT '-1',
  `isworking` tinyint(1) DEFAULT NULL,
  `chargeSum` int(11) DEFAULT '0',
  `picture` varchar(200) DEFAULT NULL,
  `workingHourFrom` int(11) DEFAULT '0',
  `workingHourTo` int(11) DEFAULT '0',
  `deviceId` varchar(20) DEFAULT NULL,
  `likeScore` int(11) DEFAULT '0',
  PRIMARY KEY (`driverId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


delimiter ;
CREATE TABLE `pre_settlement` (
  `OID` int(11) NOT NULL AUTO_INCREMENT,
  `driverId` varchar(50) NOT NULL,
  `requestTime` datetime NOT NULL,
  PRIMARY KEY (`OID`),
  KEY `fk_ps_d` (`driverId`),
  CONSTRAINT `fk_ps_d` FOREIGN KEY (`driverId`) REFERENCES `driver` (`driverId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


delimiter ;
CREATE TABLE `settlement` (
  `OID` int(11) NOT NULL,
  `TID` varchar(50) NOT NULL,
  `amount` varchar(10) NOT NULL,
  `settleTime` datetime NOT NULL,
  `method` varchar(10) NOT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `TID_UNIQUE` (`TID`),
  KEY `FK_OID` (`OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


delimiter ;
CREATE TABLE `charge_history` (
  `BID` int(11) NOT NULL AUTO_INCREMENT,
  `driverId` varchar(45) NOT NULL,
  `fee` int(11) NOT NULL,
  `businessTime` datetime DEFAULT NULL,
  `businessType` int(11) DEFAULT NULL,
  `customerDeviceId` varchar(45) DEFAULT NULL,
  `customerPhone` varchar(45) DEFAULT NULL,
  `source` varchar(100) DEFAULT NULL,
  `destination` varchar(100) DEFAULT NULL,
  `drivingCharge` int(11) DEFAULT NULL,
  PRIMARY KEY (`BID`),
  KEY `fk_ch_d` (`driverId`),
  CONSTRAINT `fk_ch_d` FOREIGN KEY (`driverId`) REFERENCES `driver` (`driverId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


delimiter ;
CREATE TABLE `recommend` (
  `RID` int(11) NOT NULL AUTO_INCREMENT,
  `customerDevId` varchar(20) NOT NULL,
  `driverId` varchar(20) NOT NULL,
  `recommendTime` varchar(45) NOT NULL,
  PRIMARY KEY (`RID`),
  KEY `FK_driverId` (`driverId`),
  CONSTRAINT `FK_driverId` FOREIGN KEY (`driverId`) REFERENCES `driver` (`driverId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


