package com.hubspot.horizon;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by anev on 02/04/16.
 */
public class RetryHelperTest {

    private HttpRequest.Options op;
    private static final int INITIAL_RETRY_BACKOFF_SEC = 12;

    @Before
    public void init() {
        op = HttpRequest.Options.DEFAULT;
        op.setInitialRetryBackoffSeconds(INITIAL_RETRY_BACKOFF_SEC);
        op.setMaxRetries(0);
    }

    @Test
    public void testNoRetry() {
        assertThat(RetryHelper.computeBackoff(op, 0))
                .describedAs("first retry should be in 3 s or less")
                .isLessThanOrEqualTo(3000);
    }

    @Test
    public void test1Retry() {
        assertThat(RetryHelper.computeBackoff(op, 1))
                .describedAs("2nd retry should be in 15 s or less")
                .isLessThanOrEqualTo(15000);
    }

    @Test
    public void test2Retry() {
        assertThat(RetryHelper.computeBackoff(op, 2))
                .describedAs("3rd retry should be in 51 s or less")
                .isLessThanOrEqualTo(51000);
    }
}
