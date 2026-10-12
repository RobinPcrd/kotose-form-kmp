/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

import org.gradle.api.Project

/**
 * The GitHub repository published artifacts point at, as `owner/name`: the POM links and the
 * phone bridge's Swift package. GitHub Actions publish from the repository running the workflow;
 * `kotoseGithubRepository` overrides it.
 */
val Project.githubRepository: String
    get() = providers.gradleProperty("kotoseGithubRepository").orNull
        ?: System.getenv("GITHUB_REPOSITORY")
        ?: "RobinPcrd/kotose-form-kmp"
