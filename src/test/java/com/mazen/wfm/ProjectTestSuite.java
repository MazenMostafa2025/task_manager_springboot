package com.mazen.wfm;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test Suite for Project Management functionality
 * 
 * This suite includes:
 * - Unit tests for Project Repository
 * - Integration tests for Project Service
 * - Unit tests for Project Controller
 * - Integration tests for Project Controller (HTTP layer)
 * - Unit tests for Project Mapper
 * 
 * To run this suite, execute: mvn test -Dtest=ProjectTestSuite
 */
@Suite
@SuiteDisplayName("Project Management Test Suite")
@SelectPackages({
    "com.mazen.wfm.repositories",
    "com.mazen.wfm.services", 
    "com.mazen.wfm.controllers",
    "com.mazen.wfm.mapper"
})
public class ProjectTestSuite {
    // This class serves as a test suite configuration
    // All test classes in the specified packages will be included
}
