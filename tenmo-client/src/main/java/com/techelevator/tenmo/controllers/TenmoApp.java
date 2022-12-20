package com.techelevator.tenmo.controllers;

import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.UserCredentials;
import com.techelevator.tenmo.services.*;
import com.techelevator.tenmo.views.CurrentBalancePage;
import com.techelevator.tenmo.views.UserCredentialsPage;
import com.techelevator.tenmo.views.MakeTransferPage;
import com.techelevator.tenmo.views.UserOutput;

public class TenmoApp
{

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final UserOutput userOutput = new UserOutput();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final AccountService accountService = new AccountService();

    private AuthenticatedUser currentUser;
    private UserService userService = new UserService();
    private TransferService transferService = new TransferService();

    public TenmoApp() {
        AuthenticatedApiService.setBaseUrl(API_BASE_URL);
    }

    public void run()
    {
        userOutput.printGreeting();
        loginMenu();
        if (currentUser != null)
        {
            mainMenu();
        }
    }

    private void loginMenu()
    {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null)
        {
            userOutput.printLoginMenu();
            menuSelection = userOutput.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1)
            {
                handleRegister();
            }
            else if (menuSelection == 2)
            {
                handleLogin();
            }
            else if (menuSelection != 0)
            {
                System.out.println("Invalid Selection");
                userOutput.pause();
            }
        }
    }

    private void handleRegister()
    {
        System.out.println("Please register a new user account");
        UserCredentials credentials = userOutput.promptForCredentials();
        if (authenticationService.register(credentials))
        {
            System.out.println("Registration successful. You can now login.");
        }
        else
        {
            userOutput.printErrorMessage();
        }
    }

    private void handleLogin()
    {
        var page = new UserCredentialsPage();
        UserCredentials credentials = page.getUserCredential("login");
        var authenticatedUser = authenticationService.login(credentials);

        if (authenticatedUser != null)
        {
            currentUser = authenticatedUser;
            AuthenticatedApiService.setAuthToken(currentUser.getToken());
        }
        else
        {
            page.printHeader("Error");
            page.printRedLine("The login credentials were incorrect");
        }
    }

    private void mainMenu()
    {
        int menuSelection = -1;
        while (menuSelection != 0)
        {
            userOutput.printMainMenu();
            menuSelection = userOutput.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1)
            {
                viewCurrentBalance();
            }
            else if (menuSelection == 2)
            {
                viewTransferHistory();
            }
            else if (menuSelection == 3)
            {
                viewPendingRequests();
            }
            else if (menuSelection == 4)
            {
                sendBucks();
            }
            else if (menuSelection == 5)
            {
                requestBucks();
            }
            else if (menuSelection == 0)
            {
                continue;
            }
            else
            {
                System.out.println("Invalid Selection");
            }
            userOutput.pause();
        }
    }

    private void viewCurrentBalance()
    {
        var page = new CurrentBalancePage();
        var balance = accountService.getCurrentBalance();
        page.displayCurrentBalance(balance);

    }

    private void viewTransferHistory()
    {
        // TODO Auto-generated method stub

    }

    private void viewPendingRequests()
    {
        // TODO Auto-generated method stub

    }

    private void sendBucks()
    {

        var page = new MakeTransferPage();
        var users = userService.getAllUsers();
        Transfer transfer = page.getTransferDetails(users);

        transferService.makeTransfer(transfer); // this returns a Transfer object but we're not doing anything with it right now
    }

    private void requestBucks()
    {

    }

//    private void displayUsers()
//    {
//        var page = new MakeTransferPage();
//        var users = userService.getAllUsers();
//        Transfer transfer = page.getTransferDetails(users);
//
//    }

}
