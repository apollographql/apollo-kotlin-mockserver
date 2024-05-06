import com.gradleup.librarian.core.SonatypeHost
import com.gradleup.librarian.core.configureDokkatooAggregate
import com.gradleup.librarian.core.configureDokkatooHtml
import com.gradleup.librarian.core.configurePublishing
import com.gradleup.librarian.core.pomMetadataFromGradleProperties
import com.gradleup.librarian.core.signingFromEnvironmentVariables
import com.gradleup.librarian.core.sonatypeFromEnvironmentVariables

val pomMetadata = pomMetadataFromGradleProperties()
configurePublishing(
    pomMetadata = pomMetadata,
    sonatype = sonatypeFromEnvironmentVariables(SonatypeHost.S01),
    signing = signingFromEnvironmentVariables()
)
configureDokkatooHtml()
configureDokkatooAggregate(pomMetadata.version, emptyList())