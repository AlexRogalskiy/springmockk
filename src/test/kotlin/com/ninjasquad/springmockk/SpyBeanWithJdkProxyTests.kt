package com.ninjasquad.springmockk

import com.ninjasquad.springmockk.example.ExampleService
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.lang.reflect.Proxy


/**
 * Tests for [@SpykBean][SpykBean] with a JDK proxy.
 *
 * @author Andy Wilkinson
 * @author JB Nizet
 */
@ExtendWith(SpringExtension::class)
class SpyBeanWithJdkProxyTests {
    @Autowired
    private lateinit var service: ExampleService

    @SpykBean
    private lateinit var repository: ExampleRepository

    @Test
    fun jdkProxyCanBeSpied() {
        // every { repository.find(any()) } returns Example("id")

        val example = service.find("id")
        assertThat(example.id).isEqualTo("id")
        verify { repository.find("id") }
    }

    @Configuration(proxyBeanMethods = false)
    @Import(ExampleService::class)
    class Config {
        @Bean
        fun dateService(): ExampleRepository {
            return Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(ExampleRepository::class.java)
            ) { proxy, method, args -> Example(args[0] as String) } as ExampleRepository
        }
    }

    class ExampleService(private val repository: ExampleRepository) {
        fun find(id: String): Example {
            return repository.find(id)
        }
    }

    interface ExampleRepository {
        fun find(id: String): Example
    }

    class Example(val id: String)
}
