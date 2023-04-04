package com.mino.bank.domain.account;

import org.hibernate.metamodel.model.convert.spi.JpaAttributeConverter;

public interface AccountRepository extends JpaAttributeConverter<Account, Long> {
}
