package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.repository.UserWalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.user.dto.UserWalletDTO;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class UserWalletService {

    private static final BigDecimal ONE_HUNDRED_PERCENT = new BigDecimal("100");

    private final UserWalletRepository userWalletRepository;

    private final StockDataProvider stockDataProvider;

    private final Clock clock;

    public UserWalletService(final UserWalletRepository userWalletRepository, final StockDataProvider stockDataProvider, final Clock clock) {
        this.userWalletRepository = userWalletRepository;
        this.stockDataProvider = stockDataProvider;
        this.clock = clock;
    }

    @Transactional
    public UserWalletDTO updateAndGetUserWallet(final String username) {
        return updateUserWalletImpl(username).toUserWalletDTO();
    }

    @Transactional
    public void updateUserWalletsAtTheEndOfTheDay(final List<String> usernames) {
        usernames.forEach(username -> {
            final UserWallet userWallet = updateUserWalletImpl(username);

            userWallet.setPreviousWalletValue(userWallet.getWalletValue());
            userWallet.setWalletPercentageChange(BigDecimal.ZERO);
            userWallet.setLastUpdateDate(LocalDateTime.now(clock));
        });
    }

    private UserWallet updateUserWalletImpl(final String username) {
        final UserWallet userWallet = userWalletRepository.findByUser_Username(username)
            .orElseThrow(() -> UserWalletNotFoundException.create(username));

        final AtomicReference<BigDecimal> stockValue = new AtomicReference<>(BigDecimal.ZERO);

        userWallet.getUserStocks().forEach(userStock -> {
            final String ticker = userStock.getTicker();
            final BigDecimal stockPrice = stockDataProvider.getStockData(ticker).stockQuote().currentPrice();
            final BigDecimal stockQuantityBigDecimal = BigDecimal.valueOf(userStock.getQuantity());
            final BigDecimal walletValueOfSpecificStock = stockPrice.multiply(stockQuantityBigDecimal);

            stockValue.set(stockValue.get().add(walletValueOfSpecificStock));
        });

        userWallet.setStockValue(stockValue.get());
        userWallet.setLastUpdateDate(LocalDateTime.now(clock));

        final BigDecimal totalWalletValue = userWallet.getStockValue().add(userWallet.getBalanceAvailable());
        userWallet.setWalletValue(totalWalletValue);

        final BigDecimal differenceBetweenCurrentWalletValueAndPreviousWalletValue = userWallet.getWalletValue().subtract(userWallet.getPreviousWalletValue());
        final BigDecimal walletPercentageChange = differenceBetweenCurrentWalletValueAndPreviousWalletValue
            .divide(userWallet.getPreviousWalletValue(), 4, RoundingMode.HALF_UP)
            .multiply(ONE_HUNDRED_PERCENT)
            .setScale(2, RoundingMode.HALF_UP);

        userWallet.setWalletPercentageChange(walletPercentageChange);

        return userWallet;
    }

}
