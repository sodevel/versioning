package net.nemerosa.versioning.git

import net.nemerosa.versioning.VersionInfo
import net.nemerosa.versioning.VersioningPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class GitVersionTest {

    @Test
    void 'Git not present'() {
        def wd = File.createTempDir('git', '')
        def project = ProjectBuilder.builder().withProjectDir(wd).build()
        new VersioningPlugin().apply(project)
        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info == VersionInfo.NONE
        assert info.build == ''
        assert info.branch == ''
        assert info.base == ''
        assert info.branchId == ''
        assert info.branchType == ''
        assert info.branch == ''
        assert info.commit == ''
        assert info.display == ''
        assert info.full == ''
        assert info.scm == 'n/a'
    }

    @Test
    void 'Git master'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'log', '--oneline', '--graph', '--decorate', '--all'
            }
            def head = repo.commitLookup('Commit 4')
            def headAbbreviated = repo.commitLookup('Commit 4', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'master'
            assert info.base == ''
            assert info.branchId == 'master'
            assert info.branchType == 'master'
            assert info.commit == head
            assert info.display == 'master'
            assert info.full == "master-${headAbbreviated}"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git feature branch'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'feature/123-great'
                commit 5
                git 'log', '--oneline', '--graph', '--decorate', '--all'
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'feature/123-great'
            assert info.base == '123-great'
            assert info.branchId == 'feature-123-great'
            assert info.branchType == 'feature'
            assert info.commit == head
            assert info.display == '123-great'
            assert info.full == "feature-123-great-${headAbbreviated}"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch: no previous tag'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'release/2.0'
                commit 5
                git 'log', '--oneline', '--graph', '--decorate', '--all'
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.0'
            assert info.full == "release-2.0-${headAbbreviated}"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

}
