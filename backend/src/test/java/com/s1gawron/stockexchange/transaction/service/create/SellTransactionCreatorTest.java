package com.s1gawron.stockexchange.transaction.service.create;

import com.s1gawron.stockexchange.shared.helper.UserStockGeneratorHelper;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.NoStockInUserWalletException;
import com.s1gawron.stockexchange.transaction.exception.NotEnoughStockException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SellTransactionCreatorTest {

    private UserWalletService userWalletServiceMock;

    private TransactionDAO transactionDAOMock;

    private SellTransactionCreator underTest;

    @BeforeEach
    void setUp() {
        userWalletServiceMock = Mockito.mock(UserWalletService.class);
        transactionDAOMock = Mockito.mock(TransactionDAO.class);
    }

    @Test
    void shouldReturnTrueWhenTransactionCanBeCreated() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"), 10);
        underTest = new SellTransactionCreator(transactionRequestDTO, userWalletServiceMock, transactionDAOMock);

        final Optional<UserStock> userStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1));
        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(userStock);

        final boolean result = underTest.canCreateTransaction();
        assertTrue(result);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotHaveStockToSell() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"), 10);
        underTest = new SellTransactionCreator(transactionRequestDTO, userWalletServiceMock, transactionDAOMock);

        assertThrows(NoStockInUserWalletException.class, () -> underTest.canCreateTransaction());
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotHaveEnoughStockToSell() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"), 9999);
        underTest = new SellTransactionCreator(transactionRequestDTO, userWalletServiceMock, transactionDAOMock);

        final Optional<UserStock> userStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1));
        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(userStock);

        assertThrows(NotEnoughStockException.class, () -> underTest.canCreateTransaction());
    }

    @Test
    void shouldCreateTransaction() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"), 10);
        underTest = new SellTransactionCreator(transactionRequestDTO, userWalletServiceMock, transactionDAOMock);

        final Optional<UserStock> userStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1));
        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(userStock);

        underTest.createTransaction();

        assertEquals(90, userStock.get().getQuantityAvailable());
        assertEquals(10, userStock.get().getQuantityBlocked());
        Mockito.verify(userWalletServiceMock, Mockito.times(1)).updateUserStock(userStock.get());
        Mockito.verify(transactionDAOMock, Mockito.times(1)).saveTransaction(Mockito.any(Transaction.class));
    }

}