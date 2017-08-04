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
        }
    },
    cookiePrefix: "gel-browser",
    expiration: 30  // Minutes passed to expire the session
};