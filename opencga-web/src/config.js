var opencga = {
    host: "bio-prod-opencgainternal-haproxy-01.gel.zone/opencga",
    version: "v1",
    assemblies: {
        1000000024: "GRCh37"
    },
    filters: {
        // fixed filters that will be sent as query params to the query when searching for files
        query: {
            // attributes: "gelStatus=READY"
        },
        // list containing some strings. Only the files containing any of this strings in the file name will be preselected in the filter
        // RD: only the vcfs that have SV, CNV, duprem.atomic.left.split.vcf.gz
        // Cancer: SV, somatic.duprem.left.split.vcf
        preselection: ["SV", "CNV", "duprem.atomic.left.split.vcf.gz", "somatic.duprem.left.split.vcf"]
    },
    cookiePrefix: "gel-browser",
    expiration: 30  // Minutes passed to expire the session
};