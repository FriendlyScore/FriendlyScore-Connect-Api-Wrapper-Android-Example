package com.connectapi.wrapper.example;

import androidx.appcompat.app.AppCompatActivity;

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
    Button create_user_token;
    Button get_list_banks;
    Button get_consent_screen_for_first_bank_list;
    Button get_bank_flow_url_for_first_bank_list;



    public String get_access_token_from_your_server(){

        /**
         *
         * Your server must use client_id and client_secret to authorize itself with the FriendlyScore Servers.
         *
         * The successful completion of authorization request will provide you with access_token.
         * This access_token is required to generate a user_token to make user related requests.
         * Your app must ask the server for the `access_token`
         *
         * To Test, you can use POSTMAN to get the access token locally on your development machine.
         */

        String access_token = "get_access_token_from_your_server";

        return access_token;
    }

    FriendlyScoreClient fsClient;

    //Your FriendlyScore client_id. Obtain from your FriendlyScore developer console.
    String clientId = "YOUR_CLIENT_ID";

    /**
     In order to initialize FriendlyScore for your user you must have the `userReference` for that user.
     The `userReference` uniquely identifies the user in your systems.
     This `userReference` can then be used to access information from the FriendlyScore [api](https://friendlyscore.com/developers/api).
     */
    public String userReference = "your_user_reference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fsClient  = createFriendlyScoreClient(Environment.PRODUCTION, clientId, get_access_token_from_your_server());


        create_user_token = (Button)findViewById(R.id.create_user_token);
        get_list_banks = (Button)findViewById(R.id.get_list_banks);
        get_consent_screen_for_first_bank_list = (Button)findViewById(R.id.get_consent_screen_for_first_bank);
        get_bank_flow_url_for_first_bank_list = (Button)findViewById(R.id.get_consent_url_for_first_bank);

        create_user_token.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fsClient.createUserToken(userReference, userAuthCallback);
            }
        });

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
                    Log.e(TAG, "You must select a bank from bank list and prvide the slug");
                }
            }
        });

        get_bank_flow_url_for_first_bank_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bankList != null) {
                    //Max Number of months in the past, data for this bank can be accessed from
                    int numberOfMonthsInPast  = bankList.get(0).bank.bank_configuration.transactions_consent_from.intValue();
                    //Max Number of months in the future, data for this bank will be available for
                    int numberOfMonthsInFuture = bankList.get(0).bank.bank_configuration.transactions_consent_to.intValue();

                    //Set the value to null for both transactionFromTimeStampInSec and transactionToTimeStampInSec to use default values
                    //If you want to provide custom period smaller than these above, then provide a timestamp in seconds
                    //If you provide these values, ensure these are identical to values when you fetched the consent screen information
                    Calendar futureCal = Calendar.getInstance();

                    futureCal.add(Calendar.MONTH,numberOfMonthsInFuture);

                    Calendar previousCal = Calendar.getInstance();

                    previousCal.add(Calendar.MONTH,(-1)*numberOfMonthsInPast);

                    fsClient.fetchBankFlowUrl(friendlyScoreUserToken, bankList.get(0).bank.slug, previousCal.getTimeInMillis()/1000, futureCal.getTimeInMillis()/1000, getBankConsentUrlListener);
                }else{
                    Log.e(TAG, "You must select a bank from bank list and prvide the slug");
                }
            }
        });

        /*

            Your app must handle redirection for
            /openbanking/code - successful authorization by user. It should have parameter 'bank_slug'
            /openbanking/error - error in authorization or user did not complete authorization. It should have parameter 'bank_slug' and 'error'

            Please look at the documentation for how to implement activity that will open automatically on redirection.

        */
    }


    /**
     * Generate a friendlyscore client
     * @param environment - SANDBOX, PRODUCTION
     * @param client_id - Client ID for  environment
     * @param access_token - access token obtained from server
     * @return
     */
    public FriendlyScoreClient createFriendlyScoreClient(Environment environment, String client_id, String access_token){
        final FriendlyScoreClient fsClient = new FriendlyScoreClient(environment, client_id, access_token);
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

    public ConnectRequestErrorHandler.ConnectRequestCallback<UserAuthSuccessResponse> userAuthCallback = new ConnectRequestErrorHandler.ConnectRequestCallback<UserAuthSuccessResponse>() {
        @Override
        public void success(Response<UserAuthSuccessResponse> response) {
            UserAuthSuccessResponse userAuthSuccessResponse = response.body();
            //Save this token to make other requests for the user
            friendlyScoreUserToken = userAuthSuccessResponse.getToken();
            Log.e(MainActivity.class.getSimpleName(), "user_token:"+friendlyScoreUserToken);
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
            Log.e(UserReferenceAuthCallback.class.getSimpleName(),e.getMessage());
        }

        @Override
        public void unexpectedError(Throwable t) {
        }
    };

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
                long numberOfMonthsInPast  = bankList.get(0).bank.bank_configuration.transactions_consent_from;
                //Max Number of months in the future, data for this bank will be available for
                long numberOfMonthsInFuture = bankList.get(0).bank.bank_configuration.transactions_consent_to;
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
            Log.e(MainActivity.class.getSimpleName(), "consent Screen Information for:"+consentScreenInformation.metadata.slug + " available" );
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
