package com.bezman.service;

import com.bezman.Reference.util.DatabaseUtil;
import com.bezman.init.DatabaseManager;
import com.bezman.model.ConnectedAccount;
import com.bezman.model.TwitchPointStorage;
import com.bezman.model.User;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

/**
 * Created by Terence on 1/21/2015.
 */
public class UserService
{

    public static void initializeRequest(HttpServletRequest request, HttpServletResponse response)
    {


    }

    public static void addUser(User user)
    {
        Session session = DatabaseManager.getSession();
        session.beginTransaction();

        try
        {
            session.save(user);
            session.flush();
        } catch (Exception e)
        {
            System.out.println("Could not save user : " + e.getMessage());
        }

        System.out.println("User ID : " + user.getId());

        session.getTransaction().commit();
        session.close();
    }

    public static User userForUsername(String username)
    {
        Session session = DatabaseManager.getSession();
        Query query = session.createQuery("from User where username = :username");
        query.setString("username", username);

        User user = (User) DatabaseUtil.getFirstFromQuery(query);

        session.close();

        return user;
    }

    public static User userForUsernameDevWars(String username)
    {
        Session session = DatabaseManager.getSession();
        Query query = session.createQuery("from User where username = :username and provider =  null");
        query.setString("username", username);

        User user = (User) DatabaseUtil.getFirstFromQuery(query);

        session.close();

        return user;
    }

    public static User userForUsernameOrNewVeteranUser(String username)
    {
        User user = UserService.userForUsername(username);

        if (user != null)
        {
            return user;
        } else {
            return UserService.createVeteranUserForUsername(username);
        }
    }

    public static User createVeteranUserForUsername(String username)
    {
        Session session = DatabaseManager.getSession();
        session.beginTransaction();

        User user = new User();
        user.setUsername(username);
        user.setVeteran(true);
        user.setProvider("TWITCH");

        session.save(user);

        session.getTransaction().commit();
        session.close();

        return user;
    }

    public static User userForEmail(String email)
    {
        Session session = DatabaseManager.getSession();
        Query query = session.createQuery("from User where email = :email");
        query.setString("email", email);

        User user = (User) DatabaseUtil.getFirstFromQuery(query);

        session.close();

        return user;
    }

    public static User getUser(int id)
    {
        User user = null;

        Session session = DatabaseManager.getSession();

        Query query = session.createQuery("from User where id = :id");
        query.setInteger("id", id);

        user = (User) DatabaseUtil.getFirstFromQuery(query);

        session.close();

        return user;
    }

    public static User getLastUser()
    {
        User returnUser = null;

        Session session = DatabaseManager.getSession();

        Query query = session.createQuery("from User order by id desc");

        returnUser = (User) DatabaseUtil.getFirstFromQuery(query);

        session.close();

        return returnUser;
    }

    public static User userForTwitchUsername(String username)
    {
        Session session = DatabaseManager.getSession();

        Query userQuery = session.createQuery("select u from User u left join u.connectedAccounts as a where (lower(substring(u.username, 1, length(u.username)-4)) = :username and u.provider = 'TWITCH') or (lower(a.username) = :username and a.provider = 'TWITCH')");
        userQuery.setString("username", username.toLowerCase());
        userQuery.setMaxResults(1);

        User user = (User) userQuery.uniqueResult();

        session.close();

        return user;
    }

    public static Integer userCount()
    {
        Long count = null;

        Session session = DatabaseManager.getSession();

        count = (Long) session.createQuery("select count(*) from User").uniqueResult();

        session.close();

        return count.intValue();
    }

    public static void addTwitchPointsToUser(User user)
    {
        Session session = DatabaseManager.getSession();

        Optional<ConnectedAccount> connectedAccount = user.getConnectedAccounts().stream()
                .filter(account ->
                        "TWITCH".equals(account.getProvider()) && account.getDisconnected() == false)
                .findFirst();

        boolean isTwitchAccount = "TWITCH".equals(user.getProvider());

        if (!connectedAccount.isPresent() && !isTwitchAccount) return;

        String username = connectedAccount.isPresent() ? connectedAccount.get().getUsername() : user.getUsername();

        TwitchPointStorage twitchPointStorage = (TwitchPointStorage) session.createQuery("from TwitchPointStorage where username = :username")
                .setString("username", username)
                .setMaxResults(1)
                .uniqueResult();

        if (twitchPointStorage != null)
        {
            user.getRanking().addPoints(twitchPointStorage.getPoints());
            user.getRanking().addXP(twitchPointStorage.getXp());

            session.delete(twitchPointStorage);
        }

        session.close();
    }

    public static List<User> searchUsers(String username)
    {
        Session session = DatabaseManager.getSession();

        List<User> users = session.createQuery("select user.id as id, user.username as username from User user where lower(username) LIKE :username")
                .setString("username", "%" + username.toLowerCase() + "%")
                .setResultTransformer(Transformers.aliasToBean(User.class))
                .list();

        session.close();

        return users;
    }

    public static boolean isUserAtLeast(User user, User.Role role)
    {
        User.Role userRole = user.getRole();

        return (userRole.ordinal() >= role.ordinal());
    }
}