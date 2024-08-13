import React from 'react';
import {useSearchParams} from "react-router-dom";
import styles from "./styles.module.css";
import {KEYS, StockListingsDTO} from "../../../dto/stock/StockListingsDTO";
import IndexCompanyRow from "./IndexCompanyRow";
import LinkButton from "../../linkButton/LinkButton";

export default function StockListingsData({index, stockListings}: { index: string | undefined, stockListings: StockListingsDTO }): React.ReactElement {
    const DEFAULT_KEY: string = 'A';

    const keys: React.JSX.Element[] = KEYS.map(key =>
        <div className={styles.stockListingKeyLIWrapper}>
            <li key={key} className={styles.stockListingKeyLI}>
                <LinkButton props={{linkTo: `/stockListings/${index}?q=${key}`, text: key}}/>
            </li>
        </div>
    );

    const [searchParams] = useSearchParams();
    const queryParam = searchParams.get('q');
    const key: string = queryParam == null ? DEFAULT_KEY : queryParam;

    const indexCompaniesByKey: React.JSX.Element[] = stockListings.indexCompanies[key]?.map(company => (
        <IndexCompanyRow key={company.ticker} company={company}/>
    )) ?? [];

    return (
        <>
            <div id={styles.stockListingKeyListWrapper}>
                <ol id={styles.stockListingKeyList}>
                    <div id={styles.listItemsContainer}>
                        {keys}
                        <div style={{clear: "both"}}></div>
                    </div>
                </ol>
            </div>

            {indexCompaniesByKey.length === 0 ? (
                <div id={styles.noResults}>No results found for the selected filter: {index}, {key}</div>
            ) : (
                <table id={styles.stockListingsTable}>
                    <thead>
                    <tr>
                        <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Ticker</th>
                        <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Name</th>
                        <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Industry</th>
                        <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}></th>
                    </tr>
                    </thead>

                    <tbody>
                    {indexCompaniesByKey}
                    </tbody>
                </table>
            )}
        </>
    );
}
