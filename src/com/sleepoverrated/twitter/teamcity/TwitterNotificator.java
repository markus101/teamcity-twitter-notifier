package com.sleepoverrated.twitter.teamcity;

import jetbrains.buildServer.Build;
import jetbrains.buildServer.notification.Notificator;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.UserPropertyInfo;
import jetbrains.buildServer.users.NotificatorPropertyKey;
import jetbrains.buildServer.users.PropertyKey;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.vcs.VcsRoot;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.*;

/**
 * This teamcity plugin lets you tweet to a twitter account per user based on
 * your notification settings. It uses OAuth and provides configuration options
 * to configure OAuth settings as well as custom messages to be sent. It allows
 * you to set notification triggers.
 *
 * Scott Cowan
 * Updated by: Mark McDowall
 */
public class TwitterNotificator implements Notificator {

    private static final String TYPE = "twitterNotifier";
    private static final String TYPE_NAME = "Twitter Notifier";
    private static final String TWITTER_ACCESS_TOKEN_KEY = "Access Token Key";
    private static final String TWITTER_ACCESS_TOKEN_SECRET = "Access Token Secret";
    private static final String TWITTER_CONSUMER_KEY = "Consumer Key";
    private static final String TWITTER_CONSUMER_SECRET = "Consumer Secret";
    private static final String TWITTER_BUILD_STARTED = "Build Started";
    private static final String TWITTER_BUILD_SUCCESSFUL = "Build Successful";
    private static final String TWITTER_BUILD_FAILED = "Build Failure";
    private static final String TWITTER_BUILD_LABELING_FAILED = "Labeling Failed";
    private static final String TWITTER_BUILD_FAILING = "Build Failing";
    private static final String TWITTER_BUILD_HANGING = "Build Hanging";
    private static final String TWITTER_BUILD_RESPONSIBILITY_CHANGED = "Responsibility Changed";
    private static final PropertyKey ACCESS_TOKEN_KEY = new NotificatorPropertyKey(TYPE, TWITTER_ACCESS_TOKEN_KEY);
    private static final PropertyKey ACCESS_TOKEN_SECRET = new NotificatorPropertyKey(TYPE, TWITTER_ACCESS_TOKEN_SECRET);
    private static final PropertyKey CONSUMER_KEY = new NotificatorPropertyKey(TYPE, TWITTER_CONSUMER_KEY);
    private static final PropertyKey CONSUMER_SECRET = new NotificatorPropertyKey(TYPE, TWITTER_CONSUMER_SECRET);
    private static final PropertyKey BUILD_STARTED = new NotificatorPropertyKey(TYPE, TWITTER_BUILD_STARTED);
    private static final PropertyKey BUILD_SUCCESSFUL = new NotificatorPropertyKey(TYPE, TWITTER_BUILD_SUCCESSFUL);
    private static final PropertyKey BUILD_FAILED = new NotificatorPropertyKey(TYPE, TWITTER_BUILD_FAILED);
    private static final PropertyKey BUILD_LABELING_FAILED = new NotificatorPropertyKey(TYPE, TWITTER_BUILD_LABELING_FAILED);
    private static final PropertyKey BUILD_FAILING = new NotificatorPropertyKey(TYPE, TWITTER_BUILD_FAILING);
    private static final PropertyKey BUILD_HANGING = new NotificatorPropertyKey(TYPE, TWITTER_BUILD_HANGING);
    private static final PropertyKey BUILD_RESPONSIBILITY_CHANGED = new NotificatorPropertyKey(TYPE, TWITTER_BUILD_RESPONSIBILITY_CHANGED);

    public TwitterNotificator(NotificatorRegistry notificatorRegistry) throws IOException {
        ArrayList<UserPropertyInfo> userProps = new ArrayList<UserPropertyInfo>();
        userProps.add(new UserPropertyInfo(TWITTER_ACCESS_TOKEN_KEY, "Access Token Key"));
        userProps.add(new UserPropertyInfo(TWITTER_ACCESS_TOKEN_SECRET, "Access Token Secret"));
        userProps.add(new UserPropertyInfo(TWITTER_CONSUMER_KEY, "Consumer Key"));
        userProps.add(new UserPropertyInfo(TWITTER_CONSUMER_SECRET, "Consumer Secret"));
        userProps.add(new UserPropertyInfo(TWITTER_BUILD_STARTED, "Build started"));
        userProps.add(new UserPropertyInfo(TWITTER_BUILD_SUCCESSFUL, "Build successful"));
        userProps.add(new UserPropertyInfo(TWITTER_BUILD_FAILED, "Build failed"));
        userProps.add(new UserPropertyInfo(TWITTER_BUILD_LABELING_FAILED, "Build labeling failed"));
        userProps.add(new UserPropertyInfo(TWITTER_BUILD_FAILING, "Build failing"));
        userProps.add(new UserPropertyInfo(TWITTER_BUILD_RESPONSIBILITY_CHANGED, "Responsibility changed"));
        //userProps.add(new UserPropertyInfo(TWITTER_BUILD_STARTED, "#BUILDNAME# started"));
        //userProps.add(new UserPropertyInfo(TWITTER_BUILD_SUCCESSFUL, "#BUILDNAME# #BUILDNUMBER# successful"));
        //userProps.add(new UserPropertyInfo(TWITTER_BUILD_FAILED, "#BUILDNAME# #BUILDNUMBER# failed"));
        //userProps.add(new UserPropertyInfo(TWITTER_BUILD_LABELING_FAILED, "#BUILDNAME# #BUILDNUMBER# labeling failed"));
        //userProps.add(new UserPropertyInfo(TWITTER_BUILD_FAILING, "#BUILDNAME# #BUILDNUMBER# failing"));
        //userProps.add(new UserPropertyInfo(TWITTER_BUILD_RESPONSIBILITY_CHANGED, "#BUILDNAME# responsibility changed"));
        notificatorRegistry.register(this, userProps);
    }

    public String formatMessage(Properties properties, String message) {
        for (Object key : properties.keySet()) {
            message = message.replaceAll((String) key, properties.getProperty((String) key));
        }
        return message;
    }

    public void notifyBuildStarted(SRunningBuild sRunningBuild, Set<SUser> sUsers) {
    	Properties props = getProperties(sRunningBuild);
    	
        System.out.println("Build started");
        for (SUser user : sUsers) {
        	sendTweet(formatMessage(props, user.getPropertyValue(BUILD_STARTED)), user);
        }
    }

    public void notifyBuildSuccessful(SRunningBuild sRunningBuild, Set<SUser> sUsers) {
    	Properties props = getProperties(sRunningBuild);
    	
        for (SUser user : sUsers) {
        	sendTweet(formatMessage(props, user.getPropertyValue(BUILD_SUCCESSFUL)), user);
        }
    }

    public void notifyBuildFailed(SRunningBuild sRunningBuild, Set<SUser> sUsers) {
    	Properties props = getProperties(sRunningBuild);

        for (SUser user : sUsers) {
        	sendTweet(formatMessage(props, user.getPropertyValue(BUILD_FAILED)), user);
        }
    }

    public void notifyLabelingFailed(Build build, VcsRoot vcsRoot, Throwable throwable, Set<SUser> sUsers) {
        Properties props = new Properties();
        props.setProperty("#BUILDNAME#", build.getFullName().split("::")[1]);
        props.setProperty("#FULLNAME#", build.getFullName());
        props.setProperty("#VCSNUMBER#", "");
        props.setProperty("#BUILDNUMBER#", build.getBuildNumber());
        
        for (SUser user : sUsers) {
        	sendTweet(formatMessage(props, user.getPropertyValue(BUILD_LABELING_FAILED)), user);
        }
    }

    public void notifyBuildFailing(SRunningBuild sRunningBuild, Set<SUser> sUsers) {
    	Properties props = getProperties(sRunningBuild);

        for (SUser user : sUsers) {
        	sendTweet(formatMessage(props, user.getPropertyValue(BUILD_FAILING)), user);
        }

    }

    public void notifyBuildProbablyHanging(SRunningBuild sRunningBuild, Set<SUser> sUsers) {
        Properties props = getProperties(sRunningBuild);
        
        for (SUser user : sUsers) {
        	sendTweet(formatMessage(props, user.getPropertyValue(BUILD_HANGING)), user);
        }
    }

    public void notifyResponsibleChanged(SBuildType sBuildType, Set<SUser> sUsers) {
        Properties props = new Properties();
        props.setProperty("#BUILDNAME#", sBuildType.getFullName().split("::")[1]);
        props.setProperty("#FULLNAME#", sBuildType.getFullName());
        
        for (SUser user : sUsers) {
            sendTweet(formatMessage(props, user.getPropertyValue(BUILD_RESPONSIBILITY_CHANGED)), user);
        }
    }
    
    private Properties getProperties(SRunningBuild sRunningBuild) {
    	Properties props = new Properties();
        props.setProperty("#BUILDNAME#", sRunningBuild.getFullName().split("::")[1]);
        props.setProperty("#FULLNAME#", sRunningBuild.getFullName());
        if(sRunningBuild.getRevisions().size()>0)
            props.setProperty("#VCSNUMBER#", sRunningBuild.getRevisions().get(0).getRevisionDisplayName());
        else
            props.setProperty("#VCSNUMBER#", "");
        props.setProperty("#BUILDNUMBER#", sRunningBuild.getBuildNumber());
        
        return props;
    }

    public String getNotificatorType() {
        return TYPE;
    }

    public String getDisplayName() {
        return TYPE_NAME;
    }
    
    public void sendTweet( String psText, SUser user )
    {
 	   try
 	   {
 			// Configuration 
 			ConfigurationBuilder confbuilder = new ConfigurationBuilder();
 			confbuilder.setOAuthAccessToken(user.getPropertyValue(ACCESS_TOKEN_KEY))
 			.setOAuthAccessTokenSecret(user.getPropertyValue(ACCESS_TOKEN_SECRET))
 			.setOAuthConsumerKey(user.getPropertyValue(CONSUMER_KEY))
 			.setOAuthConsumerSecret(user.getPropertyValue(CONSUMER_SECRET));
 			
 			System.out.println("Sending tweet: " + psText);
 			
 			// get the interaction object
 			Twitter twitter = new TwitterFactory(confbuilder.build()).getInstance();
 			// send 
 			twitter.updateStatus( psText );
 			//
 			//System.out.println("*** Response=" + status.getText() );
 	   }
 	   catch( Exception ex )
 	   {
 		  System.out.println("Send Tweet ERROR!");
 		  System.out.println( "***ERROR=" + stack2string( ex ));
 	   }
    }
    
    public String stack2string(Exception e) 
    {
       try 
       {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         e.printStackTrace(pw);
         return "------\r\n" + sw.toString() + "------\r\n";
       }
       catch(Exception e2) 
       {
         return "bad stack2string";
       }
    }
}
