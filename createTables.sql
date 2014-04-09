-- -----------------------------------------------------
-- Table `lonely_economy`.`accounts`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `lonely_economy`.`accounts` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(45) NOT NULL,
  `uuid` VARCHAR(36) NOT NULL,
  `balance` DECIMAL(13,2) UNSIGNED NOT NULL,
  `last_seen` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `username_UNIQUE` (`username` ASC),
  UNIQUE INDEX `uuid_UNIQUE` (`uuid` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `lonely_economy`.`transaction`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `lonely_economy`.`transaction` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `account_id` INT UNSIGNED NOT NULL,
  `amount` DECIMAL(13,2) NOT NULL,
  `timestamp` TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `id_idx` (`account_id` ASC),
  CONSTRAINT `account_id`
    FOREIGN KEY (`account_id`)
    REFERENCES `lonely_economy`.`accounts` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `lonely_economy`.`server_account`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `lonely_economy`.`server_account` (
  `balance` DECIMAL(13,2) NOT NULL,
  PRIMARY KEY (`balance`))
ENGINE = InnoDB;
