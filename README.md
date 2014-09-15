# KeyCloak Authenticator

This is an implementation of AbstractAccountAuthenticator for KeyCloak.  

# Installation

Create a OAuth Client in keycloak and put its keycloak.xml file in the `res/raw` directory.  In theory it should do the right thing based on what it finds there.

# What is working

If you log into an existing KeyCloak account then the Authenticator will add it to Android's known accounts.  After this you can get a reference to the account with:

````
Account account = am.getAccountsByType("org.keycloak.Account")[0];
````

from any application (which declares the correct permissions).

# What isn't working
 * Social Login
 * Error Handling
 * Removing an Account
 * Updating Auth token
 * I'm sure quite a few other things


