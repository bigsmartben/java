# Architecture Specification

## 1. Overview

This repository is organized as a **modular monolith** with clear module boundaries and a **strict dependency direction**.

- **Single HTTP entry**: `puml-web` is the only module that exposes HTTP endpoints.
- **Shared business database**: all business data lives in one shared database schema (single source of truth).
- **Primary goals**:
  - Keep domain/business logic in service modules.
  - Keep web/controller code thin.
  - Prevent accidental dependency cycles.
  - Make module boundaries enforceable by tests (ArchUnit).

All Java packages must be rooted at:

- `com.bsmartben.puml`

## 2. Modules

### 2.1 Module list

| Module | Responsibility | Notes |
|---|---|---|
| `puml-web` | HTTP entrypoint (controllers, request/response mapping, authn/authz, web concerns) | Must not access DB/Mapper directly |
| `puml-api` | Public API surface (DTOs + service facades/interfaces) | Used by `puml-web` (and potentially external modules) |
| `puml-common` | Shared utilities, constants, cross-cutting helpers | Must not depend on web or any service module |
| `puml-service-*` | Business capabilities (application/domain services, persistence via MyBatis) | Owns MyBatis mappers and transactions |

### 2.2 Dependency direction (strict)

The allowed dependency direction is:

```
         puml-web
            |
            v
         puml-api
            |
            v
     puml-service-*
            |
            v
       puml-common
```

#### Rules

1. `puml-web` **may depend only on**:
   - `puml-api` (service facades + DTOs)
   - `puml-common` (optional)

   `puml-web` must **not** depend on concrete service implementations or persistence classes.

2. `puml-api` may depend on:
   - `puml-common`

   `puml-api` must not depend on any `puml-service-*` module.

3. `puml-service-*` may depend on:
   - `puml-api` (implements facade interfaces and uses DTOs)
   - `puml-common`

4. `puml-common` must not depend on any other module.

5. No cycles across modules. No “backwards” dependencies.

## 3. Package naming & layering conventions

All code is under `com.bsmartben.puml`.

Recommended package layout per module:

- `puml-web`
  - `com.bsmartben.puml.web`
  - `com.bsmartben.puml.web.controller`
  - `com.bsmartben.puml.web.config`
  - `com.bsmartben.puml.web.security`
  - `com.bsmartben.puml.web.advice`

- `puml-api`
  - `com.bsmartben.puml.api`
  - `com.bsmartben.puml.api.facade` (service interfaces)
  - `com.bsmartben.puml.api.dto` (request/response DTOs)

- `puml-service-*`
  - `com.bsmartben.puml.<capability>`
  - `com.bsmartben.puml.<capability>.service` (implementation)
  - `com.bsmartben.puml.<capability>.mapper` (MyBatis mapper interfaces)
  - `com.bsmartben.puml.<capability>.repository` (optional, if you wrap mapper calls)
  - `com.bsmartben.puml.<capability>.domain` (optional)

- `puml-common`
  - `com.bsmartben.puml.common`
  - `com.bsmartben.puml.common.util`
  - `com.bsmartben.puml.common.exception`

Notes:
- “capability” should be a stable bounded-context-like name (e.g. `user`, `project`, `diagram`).
- Only `puml-web` contains Spring MVC controllers.

## 4. HTTP boundary: `puml-web` is the only entry

- All HTTP endpoints, request validation, authentication/authorization, and web error handling must live in `puml-web`.
- Web layer calls into **facade interfaces** defined in `puml-api`.
- Web layer communicates using **DTOs** from `puml-api`.
- Web layer must not:
  - reference `puml-service-*` classes directly
  - reference MyBatis mapper types
  - create or manage transactions

## 5. Service facade and DTO contract (`puml-api`)

### 5.1 Facades

- Facades are Java interfaces located in `com.bsmartben.puml.api.facade`.
- They define the use-cases (commands/queries) the web layer can invoke.

Example shape:

```java
package com.bsmartben.puml.api.facade;

import com.bsmartben.puml.api.dto.UserCreateRequest;
import com.bsmartben.puml.api.dto.UserDto;

public interface UserFacade {
  UserDto create(UserCreateRequest request);
}
```

### 5.2 DTOs

- DTOs are located in `com.bsmartben.puml.api.dto`.
- DTOs must be stable, serialization-friendly, and should not leak persistence concerns.

## 6. Persistence conventions (MyBatis in service modules)

### 6.1 Mapper location and style

- MyBatis mapper interfaces must live in **service modules** (`puml-service-*`).
- Use **annotation-based mappers** (preferred):
  - `@Mapper`
  - `@Select`, `@Insert`, `@Update`, `@Delete`
  - or provider annotations when needed

Example:

```java
package com.bsmartben.puml.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
  @Select("select id, username from puml_user where id = #{id}")
  UserPo findById(long id);
}
```

### 6.2 Mapper scanning

- Mapper scanning is configured via `@MapperScan`.
- Preferred location: the Spring Boot application configuration in `puml-web` (or a dedicated configuration class), scanning the mapper packages of service modules.

Example:

```java
@MapperScan(basePackages = {
  "com.bsmartben.puml.*.mapper"
})
@SpringBootApplication
public class PumlWebApplication {
  public static void main(String[] args) {
    SpringApplication.run(PumlWebApplication.class, args);
  }
}
```

Notes:
- Keep the scanning pattern precise; do not scan `com.bsmartben.puml..` indiscriminately if you can avoid it.

## 7. Transaction conventions

### 7.1 Where transactions live

- Transactions must be started at the **service-facade implementation boundary** in `puml-service-*`.
- `puml-web` must not use `@Transactional`.

### 7.2 Propagation and read/write

Conventions:

- Commands (create/update/delete):
  - `@Transactional(rollbackFor = Exception.class)`
- Queries (read-only):
  - `@Transactional(readOnly = true)`

### 7.3 Nested calls

- Service methods may call other service methods within the same service module.
- Cross-capability service calls should happen through their **facade** (in `puml-api`) to keep boundaries consistent.

## 8. Dependency enforcement via ArchUnit

Architecture rules are enforced by ArchUnit tests (recommended location: a dedicated test module or under `puml-web`/`puml-common` test sources).

### 8.1 Minimal rules to enforce

- `puml-web` depends only on `puml-api` and `puml-common`.
- `puml-api` depends only on `puml-common`.
- `puml-service-*` may depend on `puml-api` and `puml-common`, but not on `puml-web`.
- Mapper packages exist only in `puml-service-*`.
- Controllers exist only in `puml-web`.

### 8.2 Example ArchUnit sketch

```java
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class ArchitectureTest {

  private static final JavaClasses CLASSES = new ClassFileImporter()
      .importPackages("com.bsmartben.puml");

  @org.junit.jupiter.api.Test
  void web_must_not_depend_on_service_impl_or_mapper() {
    ArchRule rule = noClasses()
        .that().resideInAPackage("..web..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..mapper..", "..service..")
        .because("web must only depend on API facades/DTOs");

    rule.check(CLASSES);
  }

  @org.junit.jupiter.api.Test
  void controllers_only_in_web() {
    ArchRule rule = classes()
        .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
        .should().resideInAPackage("..web..controller..");

    rule.check(CLASSES);
  }
}
```

Adapt rules to your multi-module build by using package patterns and (optionally) including module names in test naming.

## 9. What to do when adding a new capability

1. Create a new service module `puml-service-<capability>`.
2. Define/extend facade interfaces + DTOs in `puml-api`.
3. Implement the facade in the service module.
4. Add MyBatis mappers in `com.bsmartben.puml.<capability>.mapper`.
5. Ensure `@MapperScan` includes the new mapper package.
6. Add/adjust ArchUnit rules if needed.

## 10. Non-goals

- This document does not define deployment topology.
- This document does not prescribe a specific domain modeling style (anemic vs rich domain), only module boundaries and enforceable dependencies.
