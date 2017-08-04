var opencga = {
    host: "bio-prod-opencgainternal-haproxy-01.gel.zone/opencga",
    version: "v1",
    assemblies: {
        1000000024: "GRCh37", //RD37
        1000000026: "GRCh37", //CG37
        1000000028: "GRCh37", //UN37
        1000000030: "GRCh37", //CS37
        1000000032: "GRCh38", //RD38
        1000000034: "GRCh38", //CG38
        1000000036: "GRCh38", //UN38
        1000000038: "GRCh38", //CS38
        1000000041: "GRCh38"  //CS380317
    },
    filters: {
        // fixed filters that will be sent as query params to the query when searching for files
        query: {
            "attributes.gelStatus": "=READY"
        },
        // list containing some strings. Only the files containing any of this strings in the file name will be preselected in the filter
        // RD: only the vcfs that have SV, CNV, duprem.atomic.left.split.vcf.gz
        // Cancer: SV, somatic.duprem.left.split.vcf
        preselection: ["SV", "CNV", "duprem.atomic.left.split.vcf.gz", "somatic.duprem.left.split.vcf"]
    },
    cookiePrefix: "gel-browser",
    expiration: 30  // Minutes passed to expire the session
};