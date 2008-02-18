/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.vote;

import junit.framework.TestCase;

import org.springframework.security.AccessDeniedException;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.SecurityConfig;

import org.springframework.security.providers.TestingAuthenticationToken;

import java.util.List;
import java.util.Vector;


/**
 * Tests {@link UnanimousBased}.
 *
 * @author Ben Alex
 * @version $Id$
 */
public class UnanimousBasedTests extends TestCase {

    //~ Methods ========================================================================================================

    private UnanimousBased makeDecisionManager() {
        UnanimousBased decisionManager = new UnanimousBased();
        RoleVoter roleVoter = new RoleVoter();
        DenyVoter denyForSureVoter = new DenyVoter();
        DenyAgainVoter denyAgainForSureVoter = new DenyAgainVoter();
        List voters = new Vector();
        voters.add(roleVoter);
        voters.add(denyForSureVoter);
        voters.add(denyAgainForSureVoter);
        decisionManager.setDecisionVoters(voters);

        return decisionManager;
    }

    private UnanimousBased makeDecisionManagerWithFooBarPrefix() {
        UnanimousBased decisionManager = new UnanimousBased();
        RoleVoter roleVoter = new RoleVoter();
        roleVoter.setRolePrefix("FOOBAR_");

        DenyVoter denyForSureVoter = new DenyVoter();
        DenyAgainVoter denyAgainForSureVoter = new DenyAgainVoter();
        List voters = new Vector();
        voters.add(roleVoter);
        voters.add(denyForSureVoter);
        voters.add(denyAgainForSureVoter);
        decisionManager.setDecisionVoters(voters);

        return decisionManager;
    }

    private TestingAuthenticationToken makeTestToken() {
        return new TestingAuthenticationToken("somebody", "password",
            new GrantedAuthority[] {new GrantedAuthorityImpl("ROLE_1"), new GrantedAuthorityImpl("ROLE_2")});
    }

    private TestingAuthenticationToken makeTestTokenWithFooBarPrefix() {
        return new TestingAuthenticationToken("somebody", "password",
            new GrantedAuthority[] {new GrantedAuthorityImpl("FOOBAR_1"), new GrantedAuthorityImpl("FOOBAR_2")});
    }

    public final void setUp() throws Exception {
        super.setUp();
    }

    public void testOneAffirmativeVoteOneDenyVoteOneAbstainVoteDeniesAccess() throws Exception {
        TestingAuthenticationToken auth = makeTestToken();
        UnanimousBased mgr = makeDecisionManager();

        ConfigAttributeDefinition config = new ConfigAttributeDefinition(new String[]{"ROLE_1", "DENY_FOR_SURE"});

        try {
            mgr.decide(auth, new Object(), config);
            fail("Should have thrown AccessDeniedException");
        } catch (AccessDeniedException expected) {
            assertTrue(true);
        }
    }

    public void testOneAffirmativeVoteTwoAbstainVotesGrantsAccess() throws Exception {
        TestingAuthenticationToken auth = makeTestToken();
        UnanimousBased mgr = makeDecisionManager();

        ConfigAttributeDefinition config = new ConfigAttributeDefinition("ROLE_2");

        mgr.decide(auth, new Object(), config);
        assertTrue(true);
    }

    public void testOneDenyVoteTwoAbstainVotesDeniesAccess() throws Exception {
        TestingAuthenticationToken auth = makeTestToken();
        UnanimousBased mgr = makeDecisionManager();

        ConfigAttributeDefinition config = new ConfigAttributeDefinition("ROLE_WE_DO_NOT_HAVE");

        try {
            mgr.decide(auth, new Object(), config);
            fail("Should have thrown AccessDeniedException");
        } catch (AccessDeniedException expected) {
            assertTrue(true);
        }
    }

    public void testRoleVoterPrefixObserved() throws Exception {
        TestingAuthenticationToken auth = makeTestTokenWithFooBarPrefix();
        UnanimousBased mgr = makeDecisionManagerWithFooBarPrefix();

        ConfigAttributeDefinition config = new ConfigAttributeDefinition(new String[]{"FOOBAR_1", "FOOBAR_2"});

        mgr.decide(auth, new Object(), config);
        assertTrue(true);
    }

    public void testThreeAbstainVotesDeniesAccessWithDefault() throws Exception {
        TestingAuthenticationToken auth = makeTestToken();
        UnanimousBased mgr = makeDecisionManager();

        assertTrue(!mgr.isAllowIfAllAbstainDecisions()); // check default

        ConfigAttributeDefinition config = new ConfigAttributeDefinition("IGNORED_BY_ALL");

        try {
            mgr.decide(auth, new Object(), config);
            fail("Should have thrown AccessDeniedException");
        } catch (AccessDeniedException expected) {
            assertTrue(true);
        }
    }

    public void testThreeAbstainVotesGrantsAccessWithoutDefault() throws Exception {
        TestingAuthenticationToken auth = makeTestToken();
        UnanimousBased mgr = makeDecisionManager();
        mgr.setAllowIfAllAbstainDecisions(true);
        assertTrue(mgr.isAllowIfAllAbstainDecisions()); // check changed

        ConfigAttributeDefinition config = new ConfigAttributeDefinition("IGNORED_BY_ALL");

        mgr.decide(auth, new Object(), config);
        assertTrue(true);
    }

    public void testTwoAffirmativeVotesTwoAbstainVotesGrantsAccess() throws Exception {
        TestingAuthenticationToken auth = makeTestToken();
        UnanimousBased mgr = makeDecisionManager();

        ConfigAttributeDefinition config = new ConfigAttributeDefinition(new String[]{"ROLE_1", "ROLE_2"});

        mgr.decide(auth, new Object(), config);
        assertTrue(true);
    }
}