This versions uses a POST for all form data. No data is displayed on the URL.

The button for "Update" makes use a form for POSTing data. The form passed employeeId as a hidden form field. The controller request mapping "/showFormForUpdate" was changed to use @PostMapping. All of the other code is the same.