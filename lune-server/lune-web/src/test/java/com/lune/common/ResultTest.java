package com.lune.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link Result} / {@link PageResult} / {@link BusinessException} 基础契约测试。
 */
class ResultTest {

    @Test
    void successWrapsDataWithCode200() {
        Result<String> result = Result.success("ok");
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("success");
        assertThat(result.getData()).isEqualTo("ok");
    }

    @Test
    void failUsesCode500ByDefault() {
        Result<Void> result = Result.fail("boom");
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).isEqualTo("boom");
        assertThat(result.getData()).isNull();
    }

    @Test
    void failWithExplicitCode() {
        Result<Void> result = Result.fail(404, "not found");
        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void unauthorizedAndForbidden() {
        assertThat(Result.unauthorized().getCode()).isEqualTo(401);
        assertThat(Result.forbidden().getCode()).isEqualTo(403);
    }

    @Test
    void pageResultOfPopulatesAllFields() {
        PageResult<String> page = PageResult.of(List.of("a", "b"), 42L, 3, 20);
        assertThat(page.getRecords()).containsExactly("a", "b");
        assertThat(page.getTotal()).isEqualTo(42L);
        assertThat(page.getPage()).isEqualTo(3);
        assertThat(page.getSize()).isEqualTo(20);
    }

    @Test
    void businessExceptionCarriesCode() {
        BusinessException e = new BusinessException(404, "心愿不存在");
        assertThat(e.getCode()).isEqualTo(404);
        assertThat(e.getMessage()).isEqualTo("心愿不存在");

        BusinessException defaultCode = new BusinessException("默认");
        assertThat(defaultCode.getCode()).isEqualTo(400);
    }
}
