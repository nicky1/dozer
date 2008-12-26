/*
 * Copyright 2005-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.dozer.functional_tests;

import net.sf.dozer.util.mapping.DataObjectInstantiator;
import net.sf.dozer.util.mapping.MapperIF;
import net.sf.dozer.util.mapping.NoProxyDataObjectInstantiator;
import net.sf.dozer.util.mapping.vo.SimpleObj;
import net.sf.dozer.util.mapping.vo.SimpleObjPrime;
import net.sf.dozer.util.mapping.vo.SimpleObjPrime2;
import net.sf.dozer.util.mapping.vo.TestObject;
import net.sf.dozer.util.mapping.vo.TestObjectPrime;
import net.sf.dozer.util.mapping.vo.deep.DestDeepObj;
import net.sf.dozer.util.mapping.vo.deep.SrcDeepObj;
import net.sf.dozer.util.mapping.vo.inheritance.AnotherSubClass;
import net.sf.dozer.util.mapping.vo.inheritance.AnotherSubClassPrime;
import net.sf.dozer.util.mapping.vo.perf.MyClassA;
import net.sf.dozer.util.mapping.vo.perf.MyClassB;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author tierney.matt
 * @author garsombke.franz
 * @author dmitry.buzdin
 */
public class PerformanceTest extends AbstractMapperTest {

  private static Log log = LogFactory.getLog(PerformanceTest.class);
  private static MapperIF mapper;

  private int numIters = 1; // Set this attribute to 25000 to run performance regression

  protected void setUp() throws Exception {
    super.setUp();
    if (mapper == null) {
      mapper = getMapper("dozerBeanMapping.xml");
    }
  }

  /*
   * Baseline Performance Numbers. Established with Release 2.0 - Jan 2006
   * 
   * All performance results based on 25000 numIters and jdk 1.4.2(unless otherwise specified)
   * 
   * TEST TOTAL TIME(ms) - 1/1/06
   * 
   * testMapping1 19562 testMapping2 2859 testMapping3 2782 testMapping4 8391 testMapping5 4985
   * 
   * 
   * MHT Computer - 8/12/06 
   * #1 17015 #2 1954 #3 1890 #4 5985 #5 4062
   * 
   * 
   * MHT Computer - 9/26/06 - After 2.3 release 
   * #1 19578 #2 1937 #3 1953 #4 6734 #5 4641
   * 
   * 
   * MHT Computer - 10/1/06 - 2.4 release. After removing Reflection caching to resolve general CGLIB/Proxy issues 
   * #1 29313 #2 2985 #3 3130 #4 9891 #5 7656
   * 
   * 
   * MHT Computer - 12/20/06 - 2.5 release. After adding code submitted for fixing recursive mapping infinite loop
   * problem. Due to maintaining mappedFields variable in MappingProcessor. Is this ok or better way to do it? Not sure
   * #1 31422 #2 3485 #3 3547 #4 11656 #5 8281
   * 
   * 
   * MHT Computer - 1/26/07 - 2.5 release. After making misc perf improvements to improve test case #6
   * #1 26047 #2 3375 #3 3469 #4 11672 #5 7516 #6 45850
   * 
   * 
   * MHT Computer - 2/1/07 - 2.5 release. Just prior to release of 2.5
   * #1 26266 #2 3094 #3 3203 #4 11297 #5 7453 #6 42312
   * 
   * 
   * MHT Computer - 4/2/07 - 3.2 release.
   * #1 24891 #2 3125 #3 3219 #4 10609 #5 7328 #6 45156
   * 
   * 
   * MHT Computer - 4/7/07 - 3.2.1 release.
   * jdk1.4 #1 25391 #2 3094 #3 3156 #4 10516 #5 7406 #6 44922
   * jdk1.5 #1 24016 #2 2797 #3 2890 #4 10031 #5 7125 #6 41797
   * 
   * 
   * MHT Computer - 4/19/07 - 3.2.1 release. Rebaseline test #6. Unused and Uneccessary test data setup logic was
   * skewing prior results.
   * jdk1.4 #1 25391 #2 3094 #3 3156 #4 10516 #5 7406 #6 31687
   * jdk1.5 #1 24016 #2 2797 #3 2890 #4 10031 #5 7125 #6 26265
   * 
   * 
   * MHT Computer - 4/24/07 - 3.3 release
   * jdk1.4 #1 25485 #2 2907 #3 3219 #4 10375 #5 7312 #6 33703
   * jdk1.5 #1 23172 #2 2406 #3 2750 #4 9817 #5 6771 #6 26718
   * 
   * 
   * MHT Computer - 5/16/07 - 3.4 release
   * jdk1.4 #1 27854 #2 2945 #3 3123 #4 11844 #5 8437 #6 19567
   * jdk1.5 #1 27485 #2 2532 #3 2906 #4 11297 #5 8157 #6 16797
   * 
   * 
   * MHT Computer - 7/06/07 - 4.0 release after map backed property refactor
   * jdk1.4 #1 27047 #2 3140 #3 3172 #4 12328 #5 8359 #6 33281
   * jdk1.5 #1 26484 #2 3016 #3 3062 #4 11781 #5 7906 #6 30594
   * 
   *
   * MHT Computer - 7/15/07 - 4.0 release
   * jdk1.4 #1 24078 #2 2844 #3 2891 #4 11656 #5 7797 #6 36156
   * jdk1.5 #1 24625 #2 2656 #3 2688 #4 11515 #5 7781 #6 31797
   * 
   * MHT Computer - 9/25/07 - 4.1 release
   * jdk1.4 #1 25766 #2 2938 #3 3000 #4 12078 #5 8156 #6 36031
   * jdk1.5 #1 24516 #2 2578 #3 2672 #4 11671 #5 7875 #6 32000
   * 
   * MHT Computer - 12/15/07 - 4.2 release
   * jdk1.4 #1 23937 #2 2781 #3 2781 #4 11696 #5 7687 #6 36953
   * jdk1.5 #1 24157 #2 2594 #3 2640 #4 12438 #5 7984 #6 34531
   *
   * (WinXp SP2, P4-3.20GHz, 2GB, JVM 256m) - 12/03/08 - 4.3 release
   * jdk1.4 #1 29657 #2 3766 #3 3765 #4 17344 #5 10672 #6 40595
   * jdk1.5 #1 29297 #2 3484 #3 3329 #4 20688 #5 13032 #6 36813
   * jdk1.6 #1 22579 #2 2812 #3 3297 #4 16563 #5 11735 #6 23219
   * 
   * MHT Computer (Macbook Pro, 2.4GHZ Intel Core Duo, JVM 256m) - 12/03/08 - 4.3 release
   * jdk1.4 #1 15378 #2 1868 #3 1867 #4 8463 #5 5870 #6 23802
   * jdk1.5 #1 11965 #2 1411 #3 1417 #4 6922 #5 4636 #6 15933
   * jdk1.6 #1 6672 #2 948 #3 583 #4 1911 #5 1683 #6 3643
   * 
   * MHT Computer (Macbook Pro, 2.4GHZ Intel Core Duo, JVM 256m) - 12/26/08 - 4.4 release
   * jdk1.4 #1 17584 #2 2137 #3 2096 #4 9245 #5 6611 #6 37356
   * jdk1.5 #1 13465 #2 1539 #3 1548 #4 7425 #5 5144 #6 25975
   * jdk1.6 #1 7357 #2 1353 #3 631 #4 2239 #5 1915 #6 5834
   *  
   */

  public void testMapping1() throws Exception {
    // TestObject --> TestObjectPrime
    TestObject src = testDataFactory.getInputGeneralMappingTestObject();
    runGeneric("testMapping1", src, TestObjectPrime.class, 35000);
  }

  public void testMapping2() throws Exception {
    // SimpleObject --> SimpleObjectPrime
    SimpleObj src = testDataFactory.getSimpleObj();
    runGeneric("testMapping2", src, SimpleObjPrime.class, 3600);
  }

  public void testMapping3() throws Exception {
    // SimpleObject --> SimpleObjectPrime2
    SimpleObj src = testDataFactory.getSimpleObj();
    runGeneric("testMapping3", src, SimpleObjPrime2.class, 3700);
  }

  public void testMapping4() throws Exception {
    // AnotherSubClass --> AnotherSubClassPrime (Inheritance)
    AnotherSubClass src = testDataFactory.getAnotherSubClass();
    runGeneric("testMapping4", src, AnotherSubClassPrime.class, 12000);
  }

  public void testMapping5() throws Exception {
    // SrcDeepObj --> DestDeepObj (Field Deep)
    SrcDeepObj src = testDataFactory.getSrcDeepObj();
    runGeneric("testMapping5", src, DestDeepObj.class, 13000);
  }

  // 1-2007: Test Case submitted by Dave B.
  public void testMapping6() throws Exception {
    // MyClassA --> MyClassB. Src object contains List with 500 String elements.
    MyClassA src = testDataFactory.getRandomMyClassA();
    runGeneric("testMapping6", src, MyClassB.class, 50000);
  }

  protected DataObjectInstantiator getDataObjectInstantiator() {
    return NoProxyDataObjectInstantiator.INSTANCE;
  }

  private void runGeneric(String testName, Object src, Class destClass, long maxTimeAllowed) throws Exception {
    // warm up the mapper
    mapper.map(src, destClass);

    // perform x number of additional mappings
    log.info("Begin timings for " + testName);
    StopWatch timer = new StopWatch();
    timer.start();
    for (int i = 0; i < numIters; i++) {
      mapper.map(src, destClass);
    }
    timer.stop();
    log.info("Total time for additional " + numIters + " mappings: " + timer.getTime() + " milliseconds");
    log.info("avg time for " + numIters + " mappings: " + (timer.getTime() / numIters) + " milliseconds");

    if (timer.getTime() > maxTimeAllowed) {
      log.error("Elapsed time exceeded max allowed: " + maxTimeAllowed + " Actual time: " + timer.getTime());
      fail();
    }
  }

}
