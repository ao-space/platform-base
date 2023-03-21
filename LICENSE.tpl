{{.LicenseContent }}
{{.header.license.license}}
{{ range .Groups }}
========================================================================
{{.LicenseID}} licenses
========================================================================
{{range .Deps}}
    {{.Name}} {{.Version}} {{.LicenseID}}
{{- end }}
{{ end }}
