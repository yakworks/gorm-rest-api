
//default appsetup for the application.

screens {
    organisation{
        list{
            fields = ["*", "address.*"]
        }
    }
    defaultActions {
        open {
            enabled = false
            label = "Open"
            icon = "fa fa-eye-open"
            ngClick = "show()"
            row {
                enabled = false
                ngClick = "showRow()"
            }

            show {

            }
        }
    }
}
