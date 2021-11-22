package com.connectapi.wrapper.example;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.friendlyscore.connect.api.ConnectRequestErrorHandler;
import com.friendlyscore.connect.api.Environment;
import com.friendlyscore.connect.api.FriendlyScoreClient;
import com.friendlyscore.connect.api.responselisteners.UserReferenceAuthCallback;
import com.friendlyscore.connect.models.BankAccount;
import com.friendlyscore.connect.models.BankConsent;
import com.friendlyscore.connect.models.BankFlowUrl;
import com.friendlyscore.connect.models.BankUserCodeResponse;
import com.friendlyscore.connect.models.ConsentScreenInformation;
import com.friendlyscore.connect.models.UserAuthSuccessResponse;
import com.friendlyscore.connect.models.UserBank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    public final String TAG = MainActivity.class.getSimpleName();
    String friendlyScoreUserToken = null;
    Button get_list_banks;
    Button get_consent_screen_for_first_bank_list;
    Button get_bank_flow_url_for_first_bank_list;



    public String get_user_token_from_your_server(){

        /**
         *
         * A user token can be created for the user using the `customer_id`. A `customer_id` is available after creating the user [Create Customer](https://docs.friendlyscore.com/api-reference/customers/create-customer)

         * You must then use the `customer_id` to create `user_token` [Create User Token](https://docs.friendlyscore.com/api-reference/customers/create-customer-token)
         *
         *  Your app must ask the server for the `user_token`
         */

        String user_token = "get_user_token_from_your_server";

        return user_token;
    }

    FriendlyScoreClient fsClient;


    //Redirect Uri you have set in the FriendlyScore developer console.
    //Pass this value as parameter when you request the url for the bank authorization flow.
    String redirectUriVal="com.demo.friendlyscore.connect";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fsClient  = createFriendlyScoreClient(Environment.PRODUCTION);

        friendlyScoreUserToken = get_user_token_from_your_server();
        get_list_banks = (Button)findViewById(R.id.get_list_banks);
        get_consent_screen_for_first_bank_list = (Button)findViewById(R.id.get_consent_screen_for_first_bank);
        get_bank_flow_url_for_first_bank_list = (Button)findViewById(R.id.get_consent_url_for_first_bank);

        get_list_banks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fsClient.fetchBankList(friendlyScoreUserToken, listOfBanksListener);
            }
        });

        get_consent_screen_for_first_bank_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bankList!=null) {
                    //In sandbox only Demo-Bank would work
                    //Max Number of months in the past, data for this bank can be accessed from
                    int numberOfMonthsInPast  = bankList.get(0).bank.bank_configuration.transactions_consent_from;
                    //Max Number of months in the future, data for this bank will be available for
                    int numberOfMonthsInFuture = bankList.get(0).bank.bank_configuration.transactions_consent_to;

                    //Set the value to null for both transactionFromTimeStampInSec and transactionToTimeStampInSec to use default values

                    //If you want to access transactions for smaller number of months then provide a timestamp in seconds for these variables
                    Calendar futureCal = Calendar.getInstance();

                    futureCal.add(Calendar.MONTH,numberOfMonthsInFuture);

                    Calendar previousCal = Calendar.getInstance();

                    previousCal.add(Calendar.MONTH,(-1)*numberOfMonthsInPast);

                    fsClient.fetchConsentScreenInformation(friendlyScoreUserToken, bankList.get(0).bank.slug, previousCal.getTimeInMillis()/1000, futureCal.getTimeInMillis()/1000, consentScreenCallback);
                }else{
                    Log.e(TAG, "You must select a bank from bank list and provide the slug");
                }
            }
        });

        get_bank_flow_url_for_first_bank_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bankList != null) {
                    //Max Number of months in the past, data for this bank can be accessed from
                    int numberOfMonthsInPast  = bankList.get(0).bank.bank_configuration.transactions_consent_from;
                    //Max Number of months in the future, data for this bank will be available for
                    int numberOfMonthsInFuture = bankList.get(0).bank.bank_configuration.transactions_consent_to;

                    //Set the value to null for both transactionFromTimeStampInSec and transactionToTimeStampInSec to use default values
                    //If you want to provide custom period smaller than these above, then provide a timestamp in seconds
                    //If you provide these values, ensure these are identical to values when you fetched the consent screen information
                    Calendar futureCal = Calendar.getInstance();

                    futureCal.add(Calendar.MONTH,numberOfMonthsInFuture);

                    Calendar previousCal = Calendar.getInstance();

                    previousCal.add(Calendar.MONTH,(-1)*numberOfMonthsInPast);

                    fsClient.fetchBankFlowUrl(friendlyScoreUserToken, bankList.get(0).bank.slug, previousCal.getTimeInMillis()/1000, futureCal.getTimeInMillis()/1000, redirectUriVal, getBankConsentUrlListener);
                }else{
                    Log.e(TAG, "You must select a bank from bank list and prvide the slug");
                }
            }
        });


    }

    /**
     * Generate a friendlyscore client
     * @param environment - SANDBOX, PRODUCTION
     * @return
     */
    public FriendlyScoreClient createFriendlyScoreClient(Environment environment){
        final FriendlyScoreClient fsClient = new FriendlyScoreClient(environment);
        return fsClient;

    }


    /**
     * Make this request if the user wants to withdraw consent for accessing bank account information
     * @param fsClient - FriendlyScoreClient
     * @param friendlyScoreUserToken - User Token obtained from authorization endpoint
     * @param bankSlug
     */
    public void deleteBankConsent(FriendlyScoreClient fsClient, String friendlyScoreUserToken, String bankSlug){
        fsClient.deleteBankConsent(friendlyScoreUserToken, bankSlug, bankConsentDeleteListener );
    }

    List<UserBank> bankList;
    public ConnectRequestErrorHandler.ConnectRequestCallback<List<UserBank>> listOfBanksListener = new ConnectRequestErrorHandler.ConnectRequestCallback<List<UserBank>>() {
        @Override
        public void success(Response<List<UserBank>> response) {
            bankList    = response.body();


            if (bankList!=null){
                Log.e(MainActivity.class.getSimpleName(), "Number of banks:" + bankList.size());
                //Below are the important variables available for each bank object
                //Slug is Unique per bank
                String bankSlug = bankList.get(0).bank.slug;
                //Bank Name
                String bankName = bankList.get(0).bank.name;
                //Bank CountryCode
                String bankCountryCode = bankList.get(0).bank.country_code;
                //Bank Logo Url
                String bankLogoUrl = bankList.get(0).bank.logo_url;
                //Bank Type {Personal, Business}
                String type = bankList.get(0).bank.type;
                //Max Number of months in the past, data for this bank can be accessed from
                int numberOfMonthsInPast  = bankList.get(0).bank.bank_configuration.transactions_consent_from;
                //Max Number of months in the future, data for this bank will be available for
                int numberOfMonthsInFuture = bankList.get(0).bank.bank_configuration.transactions_consent_to;
                // Status if the user has connected the with an account at the bank
                Boolean connectedBank = bankList.get(0).connected;
                //The flag when true indicates the bank APIs are available
                Boolean isActive = bankList.get(0).bank.is_active;
                //Accounts for the user, if the user has connected the account
                ArrayList<BankAccount> bankAccountList = bankList.get(0).accounts;

            }
        }

        @Override
        public void unauthenticated(Response<?> response) {
            Log.e(MainActivity.class.getSimpleName(), "unauthenticated");
        }

        @Override
        public void unauthorized(Response<?> response) {
            Log.e(MainActivity.class.getSimpleName(), "unauthorized");
        }

        @Override
        public void clientError(Response<?> response) {
            Log.e(MainActivity.class.getSimpleName(), "clientError");
        }

        @Override
        public void serverError(Response<?> response) {
            Log.e(MainActivity.class.getSimpleName(), "serverError");
        }

        @Override
        public void networkError(IOException e) {
            Log.e(MainActivity.class.getSimpleName(), "networkError");
        }

        @Override
        public void unexpectedError(Throwable t) {
        }
    };

    public ConnectRequestErrorHandler.ConnectRequestCallback<BankFlowUrl> getBankConsentUrlListener = new ConnectRequestErrorHandler.ConnectRequestCallback<BankFlowUrl>() {
        /** Called for [200, 300) responses. */
        @Override
        public void success(Response<BankFlowUrl> response) {
            BankFlowUrl bankFlowUrl = response.body();
            Log.e(MainActivity.class.getSimpleName(),bankFlowUrl.url);

            //Open the url to direct the user to the bank for authorization
        }
        /** Called for 401 responses. */
        @Override
        public void unauthenticated(Response<?> response) {

        }
        /** Called for 403 responses. */
        @Override
        public void unauthorized(Response<?> response) {

        }
        /** Called for [400, 500) responses, except 401, 403. */
        @Override
        public void clientError(Response<?> response) {

        }
        /** Called for [500, 600) response. */
        @Override
        public void serverError(Response<?> response) {

        }
        /** Called for network errors while making the call. */
        @Override
        public void networkError(IOException e) {
        }
        /** Called for unexpected errors while making the call. */
        @Override
        public void unexpectedError(Throwable t) {
        }
    };



    public ConnectRequestErrorHandler.ConnectRequestCallback<ConsentScreenInformation> consentScreenCallback = new ConnectRequestErrorHandler.ConnectRequestCallback<ConsentScreenInformation>() {
        @Override
        public void success(Response<ConsentScreenInformation> response) {
            ConsentScreenInformation consentScreenInformation = response.body();
            Log.e(MainActivity.class.getSimpleName(), "Consent Screen Information for:"+consentScreenInformation.metadata.slug + " available" );
        }

        @Override
        public void unauthenticated(Response<?> response) {

        }

        @Override
        public void unauthorized(Response<?> response) {

        }

        @Override
        public void clientError(Response<?> response) {

        }

        @Override
        public void serverError(Response<?> response) {

        }

        @Override
        public void networkError(IOException e) {
        }

        @Override
        public void unexpectedError(Throwable t) {
        }
    };

    public ConnectRequestErrorHandler.ConnectRequestCallback<Void > bankConsentDeleteListener = new ConnectRequestErrorHandler.ConnectRequestCallback<Void>() {
        @Override
        public void success(Response<Void> response) {

        }

        @Override
        public void unauthenticated(Response<?> response) {

        }

        @Override
        public void unauthorized(Response<?> response) {

        }

        @Override
        public void clientError(Response<?> response) {

        }

        @Override
        public void serverError(Response<?> response) {

        }

        @Override
        public void networkError(IOException e) {
        }

        @Override
        public void unexpectedError(Throwable t) {
        }
    };
}
