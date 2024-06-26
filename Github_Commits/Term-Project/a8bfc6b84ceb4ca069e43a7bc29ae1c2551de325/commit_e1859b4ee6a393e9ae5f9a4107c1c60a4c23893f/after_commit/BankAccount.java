import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class AccountBalance here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class BankAccount extends Actor
{

    private GreenfootImage bankaccountImage;
    public static double balance;
    // dont really need number

    /**
     * Act - do whatever the CreditScore wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    
    // creates a bank account image with a balance of 0
    public BankAccount(double balance){
        setBalance(balance);
        Font font = new Font(true, false, 20);
        bankaccountImage = new GreenfootImage(250, 50);
        bankaccountImage.setColor(Color.BLACK);
        bankaccountImage.setFont(font);
        bankaccountImage.drawString("Account Balance: \n          $"+balance, 0, 20);
        setImage(bankaccountImage);
    }

    
    // updates bank account image
    public void act()
    {
        
        bankaccountImage.setColor(Color.BLACK);
        //updateBalance();
        bankaccountImage.clear();
        bankaccountImage.drawString("Account Balance: \n         $"+balance, 0, 20);
        setImage(bankaccountImage);   
    }
    
    /*public void updateBalance(){
        this.balance = balance+character.getMoney();
        
        bankaccountImage.clear();
        bankaccountImage.drawString("Account Balance: \n          $"+ balance, 0, 20);
        setImage(bankaccountImage);
    }*/
    /*public void updateBalance(){
        setBalance(character.getMoney());
        
        bankaccountImage.clear();
        bankaccountImage.drawString("Account Balance: \n          $"+ getBalance(), 0, 20);
        setImage(bankaccountImage);
    }*/
    /*
    public void decreaseBalance(double amount){
        this.balance = balance - amount;
        
        bankaccountImage.clear();
        bankaccountImage.drawString("Account Balance: \n          $"+ this.balance, 0, 20);
        setImage(bankaccountImage);
    }*/
    public double getBalance(){
        return balance;
    }
    public void setBalance(double balance){
        this.balance = balance;
    }
    
    public void updateBalance(double amount){
        balance += amount;
    }
}

