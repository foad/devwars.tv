package com.bezman.service;

import com.bezman.Reference.Reference;
import com.bezman.init.DatabaseManager;
import com.bezman.model.*;
import org.apache.commons.io.IOUtils;
import org.fusesource.hawtbuf.DataByteArrayInputStream;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.List;

@Service
public class UserTeamService
{

    public static boolean isUserInvitedToTeam(UserTeam team, User user)
    {
        return team.getInvites().stream()
                .anyMatch(current -> current.getId() == user.getId());
    }

    public static void disbandTeam(UserTeam userTeam, Integer newOwner)
    {
        Session session = DatabaseManager.getSession();
        session.beginTransaction();

        userTeam = (UserTeam) session.merge(userTeam);

        if (newOwner != null)
        {
            User newOwnerUser = (User) session.get(User.class, newOwner);

            userTeam.setOwner(newOwnerUser);
        } else
        {
            userTeam.getInvites().stream()
                    .forEach(session::delete);

            userTeam.setOwner(null);
        }

        session.getTransaction().commit();
        session.close();
    }

    public static boolean inviteUserToTeam(User user, UserTeam userTeam)
    {
        Session session = DatabaseManager.getSession();
        session.beginTransaction();

        for (User currentUser : userTeam.getMembers())
        {
            if (currentUser.getId() == user.getId()) return false;
        }

        for (UserTeamInvite currentInvite : userTeam.getInvites())
        {
            User currentUser = currentInvite.getUser();

            if (currentUser.getId() == user.getId()) return false;
        }

        session.save(new UserTeamInvite(user, userTeam));

        session.getTransaction().commit();
        session.close();

        return true;
    }

    public static List<Game> getGamesForUserTeam(UserTeam userTeam, int page, int count)
    {
        Session session = DatabaseManager.getSession();

        List<Game> games = session.createQuery("from Game game where id in (select team.game.id from Team team where team.userTeam.id = :id)")
                .setInteger("id", userTeam.getId())
                .setMaxResults(count)
                .setFirstResult(count * (page - 1))
                .list();

        session.close();

        return games;
    }

    public static List<Game> getGamesForUserTeam(UserTeam userTeam)
    {
        Session session = DatabaseManager.getSession();

        List<Game> games = session.createQuery("from Game game where id in (select team.game.id from Team team where team.userTeam.id = :id)")
                .setInteger("id", userTeam.getId())
                .list();

        session.close();

        return games;
    }

    public static Long getNumberOfGamesPlayedForUserTeam(UserTeam userTeam)
    {
        Session session = DatabaseManager.getSession();

        Long gamesCount = (Long) session.createQuery("select count(*) from Game game where id in (select team.game.id from Team team where team.userTeam.id = :id)")
                .setInteger("id", userTeam.getId())
                .setMaxResults(1)
                .uniqueResult();

        session.close();

        return gamesCount;
    }

    public static Long getNumberOfGamesWonForUserTeam(UserTeam userTeam)
    {
        Session session = DatabaseManager.getSession();

        Long gamesCount = (Long) session.createQuery("select count(*) from Game game where id in (select team.game.id from Team team where team.userTeam.id = :id and team.win = true)")
                .setInteger("id", userTeam.getId())
                .setMaxResults(1)
                .uniqueResult();

        session.close();

        return gamesCount;
    }

    public static Long getNumberOfGamesLostForUserTeam(UserTeam userTeam)
    {
        Session session = DatabaseManager.getSession();

        Long gamesCount = (Long) session.createQuery("select count(*) from Game game where id in (select team.game.id from Team team where team.userTeam.id = :id and team.win = false)")
                .setInteger("id", userTeam.getId())
                .setMaxResults(1)
                .uniqueResult();

        session.close();

        return gamesCount;
    }

    public static HashMap<Object, Object> getStatisticsForUserTeam(UserTeam userTeam)
    {
        HashMap hashMap = new HashMap();

        hashMap.put("gamesPlayed", getNumberOfGamesPlayedForUserTeam(userTeam));
        hashMap.put("gamesWon", getNumberOfGamesWonForUserTeam(userTeam));
        hashMap.put("gamesLost", getNumberOfGamesLostForUserTeam(userTeam));

        return hashMap;
    }

    public static List<UserTeamInvite> teamsInvitedTo(User user)
    {
        List<UserTeamInvite> returnList;

        Session session = DatabaseManager.getSession();

        returnList = session.createCriteria(UserTeamInvite.class)
                .add(Restrictions.eq("user.id", user.getId()))
                .list();

        session.close();

        return returnList;
    }

    public static void changeTeamPicture(UserTeam userTeam, InputStream inputStream) throws IOException
    {
        File file = new File(Reference.TEAM_PICTURE_PATH + File.separator + userTeam.getId(), "avatar.jpg");

        if(!file.getParentFile().isDirectory())
            file.getParentFile().mkdirs();

        if(!file.exists())
            file.createNewFile();

        OutputStream outputStream = new FileOutputStream(file);

        IOUtils.copy(inputStream, outputStream);

        outputStream.flush();
        outputStream.close();
    }

    public static boolean isNameTaken(String name)
    {
        if (name.isEmpty()) return true;

        Session session = DatabaseManager.getSession();

        UserTeam userTeam = (UserTeam) session.createCriteria(UserTeam.class)
                .add(Restrictions.ilike("name", name))
                .add(Restrictions.isNotNull("owner"))
                .setMaxResults(1)
                .uniqueResult();

        session.close();

        return userTeam != null;
    }

    public static boolean isTagTaken(String name)
    {
        if (name.isEmpty()) return true;

        Session session = DatabaseManager.getSession();

        UserTeam userTeam = (UserTeam) session.createCriteria(UserTeam.class)
                .add(Restrictions.ilike("tag", name))
                .add(Restrictions.isNotNull("owner"))
                .setMaxResults(1)
                .uniqueResult();

        session.close();

        return userTeam != null;
    }

    public static boolean doesUserOwnUserTeam(User user, UserTeam userTeam)
    {
        return userTeam.getOwner().getId() == user.getId();
    }

    public static boolean doesUserHaveAuthorization(User user, UserTeam userTeam)
    {
        return doesUserOwnUserTeam(user, userTeam) || UserService.isUserAtLeast(user, User.Role.ADMIN);
    }

    public static boolean doesUserBelongToTeam(User user)
    {
        return user.getTeam() != null && user.getTeam().getOwner() != null;
    }
}
