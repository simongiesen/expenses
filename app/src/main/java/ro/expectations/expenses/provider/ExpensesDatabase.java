package ro.expectations.expenses.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ro.expectations.expenses.model.AccountType;
import ro.expectations.expenses.provider.ExpensesContract.Accounts;
import ro.expectations.expenses.provider.ExpensesContract.AccountTypes;
import ro.expectations.expenses.provider.ExpensesContract.Categories;
import ro.expectations.expenses.provider.ExpensesContract.Payees;
import ro.expectations.expenses.provider.ExpensesContract.RunningBalances;
import ro.expectations.expenses.provider.ExpensesContract.Transactions;
import ro.expectations.expenses.provider.ExpensesContract.TransactionDetails;

public class ExpensesDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "expenses.db";

    public ExpensesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create table "categories".
        db.execSQL("CREATE TABLE " + Categories.TABLE_NAME + " (" +
                Categories._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                Categories.NAME + " TEXT NOT NULL," +
                Categories.PARENT_ID + " INTEGER DEFAULT NULL," +
                "CONSTRAINT fk_parent_id FOREIGN KEY (" + Categories.PARENT_ID + ") REFERENCES " +
                Categories.TABLE_NAME + " (" + Categories._ID +
                ") ON DELETE RESTRICT ON UPDATE CASCADE)");
        db.execSQL("CREATE INDEX idx_parent_id ON " + Categories.TABLE_NAME + " (" +
                Categories.PARENT_ID + " ASC)");

        // Create table "account_types".
        db.execSQL("CREATE TABLE " + AccountTypes.TABLE_NAME + " (" +
                AccountTypes._ID + " INTEGER NOT NULL PRIMARY KEY," +
                AccountTypes.TYPE + " TEXT NOT NULL)");

        // Populate table "account_types".
        populateAccountTypes(db);

        // Create table "accounts".
        db.execSQL("CREATE TABLE " + Accounts.TABLE_NAME + " (" +
                Accounts._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                Accounts.TITLE + " TEXT NOT NULL," +
                Accounts.CURRENCY + " TEXT NOT NULL," +
                Accounts.BALANCE + " INTEGER NOT NULL DEFAULT 0," +
                Accounts.TYPE_ID + " INTEGER NOT NULL," +
                Accounts.IS_ACTIVE + " INTEGER NOT NULL DEFAULT 1," +
                Accounts.INCLUDE_INTO_TOTALS + " INTEGER NOT NULL DEFAULT 1," +
                Accounts.SORT_ORDER + " INTEGER NOT NULL DEFAULT 0," +
                Accounts.NOTE + " TEXT NOT NULL," +
                Accounts.CREATED_AT + " INTEGER NOT NULL DEFAULT 0," +
                Accounts.UPDATED_AT + " INTEGER NOT NULL DEFAULT 0," +
                Accounts.LAST_TRANSACTION_AT + " INTEGER NOT NULL DEFAULT 0," +
                Accounts.LAST_ACCOUNT_ID + " INTEGER DEFAULT NULL," +
                Accounts.LAST_CATEGORY_ID + " INTEGER DEFAULT NULL," +
                "CONSTRAINT fk_type_id FOREIGN KEY (" + Accounts.TYPE_ID + ") REFERENCES " +
                        AccountTypes.TABLE_NAME + " (" + AccountTypes._ID +
                ") ON DELETE RESTRICT ON UPDATE CASCADE," +
                "CONSTRAINT fk_last_account_id FOREIGN KEY (" + Accounts.LAST_ACCOUNT_ID + ") REFERENCES " +
                        Accounts.TABLE_NAME + " (" + Accounts._ID +
                ") ON DELETE SET NULL ON UPDATE CASCADE," +
                "CONSTRAINT fk_last_category_id FOREIGN KEY (" + Accounts.LAST_CATEGORY_ID + ") REFERENCES " +
                        Categories.TABLE_NAME + " (" + Categories._ID +
                ") ON DELETE SET NULL ON UPDATE CASCADE)");
        db.execSQL("CREATE INDEX idx_type_id ON " + Accounts.TABLE_NAME + " (" +
                Accounts.TYPE_ID + " ASC)");
        db.execSQL("CREATE INDEX idx_last_account_id ON " + Accounts.TABLE_NAME + " (" +
                Accounts.LAST_ACCOUNT_ID + " ASC)");
        db.execSQL("CREATE INDEX idx_last_category_id ON " + Accounts.TABLE_NAME + " (" +
                Accounts.LAST_CATEGORY_ID + " ASC)");

        // Populate table "accounts" with some dummy data.
        populateAccountsDummyData(db);

        // Create table "payees".
        db.execSQL("CREATE TABLE " + Payees.TABLE_NAME + " (" +
                Payees._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                Payees.NAME + " TEXT NOT NULL)");

        // Create table "running_balances"
        db.execSQL("CREATE TABLE " + RunningBalances.TABLE_NAME + " (" +
                RunningBalances._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                RunningBalances.ACCOUNT_ID + " INTEGER NOT NULL," +
                RunningBalances.TRANSACTION_ID + " INTEGER NOT NULL," +
                RunningBalances.BALANCE + " INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE UNIQUE INDEX uq_account_transaction ON " + RunningBalances.TABLE_NAME + " (" +
                RunningBalances.ACCOUNT_ID + " ASC, " + RunningBalances.TRANSACTION_ID + " ASC)");

        // Create table "transactions".
        db.execSQL("CREATE TABLE " + Transactions.TABLE_NAME + " (" +
                Transactions._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                Transactions.FROM_ACCOUNT_ID + " INTEGER," +
                Transactions.TO_ACCOUNT_ID + " INTEGER," +
                Transactions.PAYEE_ID + " INTEGER DEFAULT NULL," +
                Transactions.OCCURRED_AT + " INTEGER NOT NULL DEFAULT 0," +
                Transactions.CREATED_AT + " INTEGER NOT NULL DEFAULT 0," +
                Transactions.UPDATED_AT + " INTEGER NOT NULL DEFAULT 0," +
                Transactions.NOTE + " TEXT NOT NULL," +
                Transactions.ORIGINAL_FROM_CURRENCY + " TEXT DEFAULT NULL," +
                Transactions.ORIGINAL_FROM_AMOUNT + " INTEGER DEFAULT NULL," +
                "CONSTRAINT fk_from_account_id FOREIGN KEY (" + Transactions.FROM_ACCOUNT_ID + ") REFERENCES " +
                        Transactions.TABLE_NAME + " (" + Transactions._ID +
                ") ON DELETE RESTRICT ON UPDATE CASCADE," +
                "CONSTRAINT fk_to_account_id FOREIGN KEY (" + Transactions.TO_ACCOUNT_ID + ") REFERENCES " +
                        Transactions.TABLE_NAME + " (" + Transactions._ID +
                ") ON DELETE RESTRICT ON UPDATE CASCADE," +
                "CONSTRAINT fk_payee_id FOREIGN KEY (" + Transactions.PAYEE_ID + ") REFERENCES " +
                        Transactions.TABLE_NAME + " (" + Transactions._ID +
                ") ON DELETE SET NULL ON UPDATE CASCADE)");

        db.execSQL("CREATE INDEX idx_from_account_id ON " + Transactions.TABLE_NAME +
                " (" + Transactions.FROM_ACCOUNT_ID + " ASC)");
        db.execSQL("CREATE INDEX idx_to_account_id ON " + Transactions.TABLE_NAME +
                " (" + Transactions.TO_ACCOUNT_ID + " ASC)");
        db.execSQL("CREATE INDEX idx_payee_id ON " + Transactions.TABLE_NAME +
                " (" + Transactions.PAYEE_ID + " ASC)");
        db.execSQL("CREATE INDEX idx_occurred_at ON " + Transactions.TABLE_NAME +
                " (" + Transactions.OCCURRED_AT + " ASC)");

        // Create table "transaction_details".
        db.execSQL("CREATE TABLE " + TransactionDetails.TABLE_NAME + " (" +
                TransactionDetails._ID + " INTEGER NOT NULL," +
                TransactionDetails.TRANSACTION_ID + " INTEGER NOT NULL," +
                TransactionDetails.FROM_AMOUNT + " INTEGER," +
                TransactionDetails.TO_AMOUNT + " INTEGER," +
                TransactionDetails.CATEGORY_ID + " INTEGER," +
                TransactionDetails.IS_TRANSFER + " INTEGER NOT NULL DEFAULT 0," +
                TransactionDetails.IS_SPLIT + " INTEGER NOT NULL DEFAULT 0," +
                "PRIMARY KEY(" + TransactionDetails._ID + ")," +
                "CONSTRAINT fk_transaction_id FOREIGN KEY (" + TransactionDetails.TRANSACTION_ID + ") REFERENCES " +
                        Transactions.TABLE_NAME + " (" + Transactions._ID +
                ") ON DELETE CASCADE ON UPDATE CASCADE," +
                "CONSTRAINT fk_category_id FOREIGN KEY (" + TransactionDetails.CATEGORY_ID + ") REFERENCES " +
                        Categories.TABLE_NAME + " (" + Categories._ID +
                ") ON DELETE SET NULL ON UPDATE CASCADE)");

        db.execSQL("CREATE INDEX idx_transaction_id ON " + TransactionDetails.TABLE_NAME +
                " (" + TransactionDetails.TRANSACTION_ID + " ASC)");

        db.execSQL("CREATE INDEX idx_type ON " + TransactionDetails.TABLE_NAME + " (" +
                TransactionDetails.IS_TRANSFER + " ASC, " +
                TransactionDetails.IS_SPLIT + " ASC, " +
                TransactionDetails.CATEGORY_ID + " ASC)");

        db.execSQL("CREATE INDEX idx_split ON " + TransactionDetails.TABLE_NAME +
                " (" + TransactionDetails.IS_SPLIT + " ASC)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void populateAccountTypes(SQLiteDatabase db) {
        db.beginTransaction();
        for (AccountType type : AccountType.values()) {
            ContentValues accountTypeContentValues = new ContentValues();
            accountTypeContentValues.put(AccountTypes._ID, type.ordinal());
            accountTypeContentValues.put(AccountTypes.TYPE, type.toString());
            db.insert(AccountTypes.TABLE_NAME,
                    null,
                    accountTypeContentValues
            );
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void populateAccountsDummyData(SQLiteDatabase db) {

        long unixTime = System.currentTimeMillis() / 1000L;

        db.beginTransaction();

        ContentValues cashAccountRONValues = new ContentValues();
        cashAccountRONValues.put(Accounts.TITLE, "Cash RON");
        cashAccountRONValues.put(Accounts.CURRENCY, "RON");
        cashAccountRONValues.put(Accounts.BALANCE, 75075);
        cashAccountRONValues.put(Accounts.TYPE_ID, AccountType.CASH.ordinal());
        cashAccountRONValues.put(Accounts.IS_ACTIVE, 1);
        cashAccountRONValues.put(Accounts.INCLUDE_INTO_TOTALS, 1);
        cashAccountRONValues.put(Accounts.NOTE, "");
        cashAccountRONValues.put(Accounts.CREATED_AT, unixTime);
        db.insert(Accounts.TABLE_NAME,
                null,
                cashAccountRONValues
        );

        ContentValues cashAccountEURValues = new ContentValues();
        cashAccountEURValues.put(Accounts.TITLE, "Cash EUR");
        cashAccountEURValues.put(Accounts.CURRENCY, "EUR");
        cashAccountEURValues.put(Accounts.BALANCE, 15025);
        cashAccountEURValues.put(Accounts.TYPE_ID, AccountType.CASH.ordinal());
        cashAccountEURValues.put(Accounts.IS_ACTIVE, 1);
        cashAccountEURValues.put(Accounts.INCLUDE_INTO_TOTALS, 1);
        cashAccountEURValues.put(Accounts.NOTE, "");
        cashAccountEURValues.put(Accounts.CREATED_AT, unixTime);
        db.insert(Accounts.TABLE_NAME,
                null,
                cashAccountEURValues
        );

        ContentValues cashAccountUSDValues = new ContentValues();
        cashAccountUSDValues.put(Accounts.TITLE, "Cash USD");
        cashAccountUSDValues.put(Accounts.CURRENCY, "USD");
        cashAccountUSDValues.put(Accounts.BALANCE, 1500);
        cashAccountUSDValues.put(Accounts.TYPE_ID, AccountType.CASH.ordinal());
        cashAccountUSDValues.put(Accounts.IS_ACTIVE, 0);
        cashAccountUSDValues.put(Accounts.INCLUDE_INTO_TOTALS, 1);
        cashAccountUSDValues.put(Accounts.NOTE, "");
        cashAccountUSDValues.put(Accounts.CREATED_AT, unixTime);
        db.insert(Accounts.TABLE_NAME,
                null,
                cashAccountUSDValues
        );

        ContentValues visaRON = new ContentValues();
        visaRON.put(Accounts.TITLE, "VISA RON");
        visaRON.put(Accounts.CURRENCY, "RON");
        visaRON.put(Accounts.BALANCE, 150399);
        visaRON.put(Accounts.TYPE_ID, AccountType.DEBIT_CARD.ordinal());
        visaRON.put(Accounts.IS_ACTIVE, 1);
        visaRON.put(Accounts.INCLUDE_INTO_TOTALS, 1);
        visaRON.put(Accounts.NOTE, "");
        visaRON.put(Accounts.CREATED_AT, unixTime);
        db.insert(Accounts.TABLE_NAME,
                null,
                visaRON
        );

        ContentValues visaEUR = new ContentValues();
        visaEUR.put(Accounts.TITLE, "VISA EUR");
        visaEUR.put(Accounts.CURRENCY, "EUR");
        visaEUR.put(Accounts.BALANCE, 17550);
        visaEUR.put(Accounts.TYPE_ID, AccountType.DEBIT_CARD.ordinal());
        visaEUR.put(Accounts.IS_ACTIVE, 1);
        visaEUR.put(Accounts.INCLUDE_INTO_TOTALS, 1);
        visaEUR.put(Accounts.NOTE, "");
        visaEUR.put(Accounts.CREATED_AT, unixTime);
        db.insert(Accounts.TABLE_NAME,
                null,
                visaEUR
        );

        ContentValues mastercardRON = new ContentValues();
        mastercardRON.put(Accounts.TITLE, "MasterCard RON");
        mastercardRON.put(Accounts.CURRENCY, "RON");
        mastercardRON.put(Accounts.BALANCE, 2500);
        mastercardRON.put(Accounts.TYPE_ID, AccountType.CREDIT_CARD.ordinal());
        mastercardRON.put(Accounts.IS_ACTIVE, 1);
        mastercardRON.put(Accounts.INCLUDE_INTO_TOTALS, 1);
        mastercardRON.put(Accounts.NOTE, "");
        mastercardRON.put(Accounts.CREATED_AT, unixTime);
        db.insert(Accounts.TABLE_NAME,
                null,
                mastercardRON
        );

        ContentValues mastercardEUR = new ContentValues();
        mastercardEUR.put(Accounts.TITLE, "MasterCard EUR");
        mastercardEUR.put(Accounts.CURRENCY, "EUR");
        mastercardEUR.put(Accounts.BALANCE, 0);
        mastercardEUR.put(Accounts.TYPE_ID, AccountType.CREDIT_CARD.ordinal());
        mastercardEUR.put(Accounts.IS_ACTIVE, 1);
        mastercardEUR.put(Accounts.INCLUDE_INTO_TOTALS, 1);
        mastercardEUR.put(Accounts.NOTE, "");
        mastercardEUR.put(Accounts.CREATED_AT, unixTime);
        db.insert(Accounts.TABLE_NAME,
                null,
                mastercardEUR
        );

        ContentValues savingsRON = new ContentValues();
        savingsRON.put(Accounts.TITLE, "Savings RON");
        savingsRON.put(Accounts.CURRENCY, "RON");
        savingsRON.put(Accounts.BALANCE, 2730178);
        savingsRON.put(Accounts.TYPE_ID, AccountType.BANK_ACCOUNT.ordinal());
        savingsRON.put(Accounts.IS_ACTIVE, 1);
        savingsRON.put(Accounts.INCLUDE_INTO_TOTALS, 1);
        savingsRON.put(Accounts.NOTE, "");
        savingsRON.put(Accounts.CREATED_AT, unixTime);
        db.insert(Accounts.TABLE_NAME,
                null,
                savingsRON
        );

        ContentValues savingsEUR = new ContentValues();
        savingsEUR.put(Accounts.TITLE, "Savings EUR");
        savingsEUR.put(Accounts.CURRENCY, "EUR");
        savingsEUR.put(Accounts.BALANCE, 150245);
        savingsEUR.put(Accounts.TYPE_ID, AccountType.BANK_ACCOUNT.ordinal());
        savingsEUR.put(Accounts.IS_ACTIVE, 1);
        savingsEUR.put(Accounts.INCLUDE_INTO_TOTALS, 1);
        savingsEUR.put(Accounts.NOTE, "");
        savingsEUR.put(Accounts.CREATED_AT, unixTime);
        db.insert(Accounts.TABLE_NAME,
                null,
                savingsEUR
        );

        ContentValues houseLoaning = new ContentValues();
        houseLoaning.put(Accounts.TITLE, "House Loaning");
        houseLoaning.put(Accounts.CURRENCY, "EUR");
        houseLoaning.put(Accounts.BALANCE, 175090);
        houseLoaning.put(Accounts.TYPE_ID, AccountType.BANK_ACCOUNT.ordinal());
        houseLoaning.put(Accounts.IS_ACTIVE, 1);
        houseLoaning.put(Accounts.INCLUDE_INTO_TOTALS, 0);
        houseLoaning.put(Accounts.NOTE, "");
        houseLoaning.put(Accounts.CREATED_AT, unixTime);
        db.insert(Accounts.TABLE_NAME,
                null,
                houseLoaning
        );

        db.setTransactionSuccessful();
        db.endTransaction();
    }
}
