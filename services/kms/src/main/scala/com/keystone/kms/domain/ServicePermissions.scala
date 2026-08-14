package com.keystone.kms.domain

/** Representative of the list of permissions assigned to each service, specifically
  * to convert entries into a human-readable format for easy debugging downstream. The
  * service permissions will be assigned per service and are mutually exclusive. The following
  * parameters are expected:
  * - serviceName: String - the service requesting usage of a particular key
  * - allowedOperations: Set[KeyOperation] - the set of operations the service is allowed to perform
  */
case class ServicePermissions(
                               serviceName: String, 
                               allowedOperations: Set[KeyOperation] = Set.empty)
