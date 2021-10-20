### Introduction

FriendlyScore Connect API Wrapper allows you build custom UX to connect bank accounts by using FriendlyScore REST API.

### Requirements

  - Install or update Android Studio to version 3.2 or greater
  - We support Android 5.0 and greater
  - [FriendlyScore Client Id & Secret](https://friendlyscore.com/company/keys). **DO NOT put your `Client Secret`** in your mobile app.


### QuickStart
  The easiest way to get started is to clone the repository https://github.com/FriendlyScore/FriendlyScore-Connect-Api-Wrapper-Android-Example. Please follow the instructions below to provide the necessary configuration and to understand the flow.

### Getting Set up

#### Add the following values to your Project Level build.gradle file
  In your project-level Gradle file (build.gradle), add rules to include the Android Gradle plugin. The version should be equal to or greater than `3.5.3`. You must add
  `jitpack` to your repositories as declared below

    buildscript {
        ...
        dependencies {
            classpath 'com.android.tools.build:gradle:3.5.3'
        }
    } 
    
    allprojects {
        repositories {
            maven { url 'https://jitpack.io' }
            
        }
    }
### FriendlyScore Configuration

#### Add the following values to your App Level build.gradle file(In the demo app/build.gradle)
  Now we must read the configuration to create the string resources that will be used by the FriendlyScore Android SDK.

    android {
      ...
      compileOptions {
      sourceCompatibility 1.8
      targetCompatibility 1.8
      }
    }

#### Add FriendlyScore Android Framework to your app
  In your module or app-level gradle file(In the demo `app/build.gradle`) please add the FriendlyScore Android SDK library listed below to your list of dependencies

    dependencies {
       ...
       implementation 'com.github.friendlyscore:friendlyscore-android-connect-api-wrapper:0.1.2'
    }

### Integrating with FriendlyScore

You can select which environment you want to use the FriendlyScore SDK

  | Environment |   Description |
  | :----       | :--             |
  | sandbox     | Use this environment to test your integration |
  | production  | Production API environment |

These environments are listed in the SDK as below

    Environments.SANDBOX
    Environments.PRODUCTION

Choose the correct [FriendlyScore Client Id & Secret ](https://friendlyscore.com/company/keys) based on the environment you are using.

#### Steps

1. Get access token


    Your server must use `client_id` and `client_secret` to authorize itself with the FriendlyScore Servers.

    The successful completion of authorization request will provide you with `access_token`.

    This access_token is required to generate a `userToken` to make user related requests.

    Your app must ask your server for the `access_token`

    **DO NOT put your `client_secret`** in your mobile app.

    You can look at the [API reference](https://friendlyscore.com/developers/api-reference#post-/oauth/v2/token) on how to get the access token

&nbsp;
&nbsp;

2. Create the `FriendlyScoreClient`

    Choose the `client_id` and the environment you want to integrate. You can access the `client_id` from the FriendlyScore panel


        /**
        * 
        * @param environment
        * @param client_id
        * @param access_token
        */
        
        Environment environment = Environment.SANDBOX
        String client_id = "YOUR_CLIENT_ID"
        String access_token = "access_token_from_step_1"

        final FriendlyScoreClient fsClient = createFriendlyScoreClient(environment, client_id, access_token);


    The `fsClient` will be required to make other requests

&nbsp;
&nbsp;

3. Create User Token

    You must create `userToken` in order to make any request for the user.

    In order to receive response you must implement the `ConnectRequestCallback<UserAuthSuccessResponse>`.


        public ConnectRequestErrorHandler.ConnectRequestCallback<UserAuthSuccessResponse> userAuthCallback = new ConnectRequestErrorHandler.ConnectRequestCallback<UserAuthSuccessResponse>() {

            @Override
            public void success(Response<UserAuthSuccessResponse> response) {
                UserAuthSuccessResponse userAuthSuccessResponse = response.body();
                //Save this token to make other requests for the user
                userToken = userAuthSuccessResponse.getToken();

            }

            @Override
            public void unauthenticated(Response<?> response) {
                //Status Code 401
            }

            @Override
            public void unauthorized(Response<?> response) {
                //Status Code 403
            }

            @Override
            public void clientError(Response<?> response) {
                //Status Code [400, 500) expect 401 & 403
            }

            @Override
            public void serverError(Response<?> response) {
                //Status Code 500
            }

            @Override
            public void networkError(IOException e) {

            }

            @Override
            public void unexpectedError(Throwable t) {
            }
        };
    
    &nbsp;
    &nbsp;
    #### **Required parameters:**
    

    `userReference` - Unique user reference that identifies user in your systems.

    `userAuthListener` - ConnectRequestCallback<UserAuthSuccessResponse>
        
    Use the FriendlyScoreClient to make the requests

        fsClient.createUserToken(userReference, userAuthCallback);

&nbsp;
&nbsp;

4. Get List of Banks

    You can obtain the list of banks for the user

    In order to receive response you must implement the `ConnectRequestCallback<List<UserBank>>`

    
        public ConnectRequestErrorHandler.ConnectRequestCallback<List<UserBank>> listOfBanksListener = new ConnectRequestErrorHandler.ConnectRequestCallback<List<UserBank>>() {
            @Override
            public void success(Response<List<UserBank>> response) {
                List<UserBank> bankList = response.body();
                if (bankList!=null){
                    //Sample variables for 1st bank from the bank list         
                    String bankSlug = bankList.get(0).bank.slug;
                    Log.e(MainActivity.class.getSimpleName(), bankSlug);
                    //Max Number of months in the past, data for this bank can be accessed from
                    int numberOfMonthsInPast  = bankList.get(0).bank.bank_configuration.transactions_consent_from;
                    //Max Number of months in the future, data for this bank will be available for
                    int numberOfMonthsInFuture bankList.get(0).bank.bank_configuration.transactions_consent_to;
                    
                }
            }

            ...
        };
    
    &nbsp;
    &nbsp;
    #### **Required parameters:**

    `userToken` - User Token obtained from authorization endpoint

    `listOfBanksListener` - ConnectRequestErrorHandler.ConnectRequestCallback<List<UserBank>>

        fsClient.fetchBankList(userToken, listOfBanksListener);


    The important values for each bank that will be required for the ui and future requests. For example, for the first bank in the list, we show the important values:


            //Slug is Unique per bank
            String bankSlug = bankList.get(0).bank.slug;
            //Bank Name
            String bankName = bankList.get(0).bank.name;
            //Bank Logo Url
            String bankLogoUrl = bankList.get(0).bank.logo_url;
            //Bank CountryCode
            String bankCountryCode = bankList.get(0).bank.country_code;
            //Bank Type {Personal, Business}
            String type = bankList.get(0).bank.type;
            //Max Number of months in the past, data for this bank can be accessed from
            int numberOfMonthsInPast  = bankList.get(0).bank.bank_configuration.transactions_consent_from;
            //Max Number of months in the future, data for this bank will be available for
            int numberOfMonthsInFuture bankList.get(0).bank.bank_configuration.transactions_consent_to;
            // Status if the user has connected the with an account at the bank
            Boolean connectedBank = bankList.get(0).connected
            //The flag when true indicates the bank APIs are available
            Boolean isActive = bankList.get(0).bank.is_active
            //Accounts for the user, if the user has connected the account
            ArrayList<BankAccount> bankAccountList = bankList.get(0).accounts;

    The `bankSlug` value in the code block above is used across all the endpoints to build the rest of the user journey.


&nbsp;
&nbsp;

5. Get Bank Consent Screen Information

    Once the user has selected a bank from the list. You must show the user the necessary information as required by the law.

    In order to receive response you must implement the `ConnectRequestCallback<ConsentScreenInformation>`

   &nbsp;
   &nbsp;

       
            public ConnectRequestErrorHandler.ConnectRequestCallback<ConsentScreenInformation> consentScreenCallback = new ConnectRequestErrorHandler.ConnectRequestCallback<ConsentScreenInformation>() {
                @Override
                public void success(Response<ConsentScreenInformation> response) {
                    
                }   
            ...
            }


    &nbsp;
    &nbsp;
    #### **Required parameters:**


    `userToken` - User Token obtained from authorization endpoint

    `bankSlug` - Slug for the bank user has selected from the list of banks

    `transactionFromTimeStampInSec`  - Set to null to use default values.

    `transactionToTimeStampInSec`  - Set to null to use default values.

    `consentScreenCallback` - ConnectRequestErrorHandler.ConnectRequestCallback<ConsentScreenInformation>
            

        fsClient.fetchConsentScreenInformation(userToken, bankSlug, transactionFromTimeStampInSec, transactionToTimeStampInSec, consentScreenCallback);


    The ConsentScreenInformation includes 2 objects `metadata` and `consents`. You can use information in `metadata` to build your custom consent information text. The `consents` object provides ready-to-use text to build the consent screen.

&nbsp;
&nbsp;

6. Get Bank Flow Url

    Make this request from the consent screen after the user has seen all the information that will is being requested.

    You must make this request to get the url to open the Bank Flow for users to authorize access account information.

    You need the `redirectUri` so FriendlyScore can redirect the user back to your app. This must be the scheme you are using to bring the user back to your app.
    It must be the same as set in the FriendlyScore developer console and AndroidManifest.xml while declaring the activity.

    In order to receive response you must implement the `ConnectRequestCallback<BankFlowUrl>`

    &nbsp;
    &nbsp;
    #### **Required parameters:** 


    `fsClient` - FriendlyScoreClient
        
    `userToken` - User Token obtained from authorization endpoint
        
    `bankSlug` - Slug for the bank user has selected from the list of banks

    `transactionFromTimeStampInSec` - Time stamp in seconds. Set to null to use default

    `transactionToTimeStampInSec` - Time stamp in seconds. Set to null to use default.

    `redirectUri` - This must be the scheme you are using to bring the user back to your app. It must be the same as set in the FriendlyScore developer console and AndroidManifest.xml while declaring the activity.

    `bankFlowUrlListener` - ConnectRequestErrorHandler.ConnectRequestCallback<BankFlowUrl> 

         
    &nbsp;
    &nbsp;
    
    
        public ConnectRequestErrorHandler.ConnectRequestCallback<BankFlowUrl> bankFlowUrlListener = new ConnectRequestErrorHandler.ConnectRequestCallback<BankFlowUrl>() {
            /** Called for [200, 300) responses. */
            @Override
            public void success(Response<BankFlowUrl> response) {
                    BankFlowUrl bankFlowUrl = response.body();
                    Log.e(MainActivity.class.getSimpleName(),bankFlowUrl.url);
            }
            ...
        }
        
    &nbsp;
    &nbsp;
    #### **Required parameters:** 

        
    `userToken` - User Token obtained from authorization endpoint
        
    `bankSlug` - Slug for the bank user has selected from the list of banks

    `transactionFromTimeStampInSec` - Time stamp in seconds. Set to null to use default

    `transactionToTimeStampInSec` - Time stamp in seconds. Set to null to use default.

    `bankFlowUrlListener` - ConnectRequestErrorHandler.ConnectRequestCallback<BankFlowUrl> 
  
  

        fsClient.fetchBankFlowUrl(userToken, bankSlug, transactionFromTimeStampInSec, transactionToTimeStampInSec, redirectUri, bankFlowUrlListener);
    


    From `BankFlowUrl` extract the `url` value and trigger it to start the authorization process with the bank


&nbsp;
&nbsp;


7. Redirect back to the app

    Go to the Redirects section of the [FriendlyScore developer console](https://friendlyscore.com/company/keys) and provide your `Android Scheme` and `Android Package`

    The user is redirected back to your app after a user successfully authorizes, cancels the authorization process or any other error during the authorization.

    &nbsp;
    &nbsp;

    Your app must handle redirection for

    &nbsp;
    &nbsp;

    `/openbanking/code` - successful authorization by user. It should have parameters `bank_slug`

    &nbsp;
    &nbsp;

    `/openbanking/error` - error in authorization or user did not complete authorization. It should have 2 parameters. `bank_slug` and `error` 
    
    &nbsp;
    &nbsp;
    
    In order to handle redirection your App manifest `AndroidManifest.xml` must have activity that is listening for this redirection.

    
    Your activity must declare `intent-filter` tag with the `action` and `category` as shown below

    the `data` tag must declare `android:host`, `android:path` and `android:scheme`.
    
    &nbsp;
    &nbsp;
    
    **ONLY edit `android:scheme`** to point to your app. This value must be the same as the value you set in the developer section of FriendlyScore Console. 

    &nbsp;
    &nbsp;

    **DO NOT change `android:host` and `android:path`, these values must be as is below in the code blocks.**

    &nbsp;
    &nbsp;

    For redirection after successful bank account authorization, create an activity with declaration in your manifest as below

        <activity
            android:name="BankConnectThankYouRedirect"
            >
            <intent-filter android:label="">
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data
                    android:host="api.friendlyscore.com"
                    android:path="/openbanking/code"
                    android:scheme="com.my.demo.app" />
            </intent-filter>
        </activity>

    &nbsp;
    &nbsp;
    
    For redirection after user cancels or any other error in completing bank account authorization, create an activity with declaration in your manifest as below

        <activity
            android:name="BankConnectErrorRedirect"
            >
            <intent-filter android:label="">
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data
                    android:host="api.friendlyscore.com"
                    android:path="/openbanking/error"
                    android:scheme="com.my.demo.app" />
            </intent-filter>
        </activity>


&nbsp;
&nbsp;

8. Delete Account Consent

    Make this request to allow the user to delete consent to access account information.


    In order to receive response you must implement the `ConnectRequestErrorHandler.ConnectRequestCallback<Void>`



        public ConnectRequestErrorHandler.ConnectRequestCallback<Void > bankConsentDeleteListener = new ConnectRequestErrorHandler.ConnectRequestCallback<Void>() {

        @Override
            public void success(Response<Void> response) {

            }
            ...
        }
  
      &nbsp;
      &nbsp;

    #### **Required parameters:** 
    `fsClient` - FriendlyScoreClient

    `userToken` - User Token obtained from authorization endpoint
        
    `bankSlug` - Slug for the bank user has selected from the list of banks

        fsClient.deleteBankConsent(userToken, bankSlug, bankConsentDeleteListener );
