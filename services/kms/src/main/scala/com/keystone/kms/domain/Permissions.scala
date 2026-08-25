package com.keystone.kms.domain

import java.time.Instant
import scala.annotation.tailrec

/** Representative of the permissions allowed on each key; each permission documents
  * the respective allowed operations by service and each operation is mutually
  * exclusive. The structure of permissions is as follows:
  * - entries: List[ServicePermissions] - a list of each permission allowed by each entity
  * - lastUpdated: Instant - the timestamp for when the record was last updated
  * - changeHistory: List[PermissionChange] - an audit trail of all permission change
  *   requests presented for a respective key
  */
case class Permissions private (
                               entries: List[ServicePermissions],
                               lastUpdated: Instant,
                               changeHistory: List[PermissionChange])

object Permissions:
    def create(): Permissions =
        Permissions(
            entries=List.empty,
            lastUpdated=Instant.now(),
            changeHistory=List.empty)

    /** Recursive helper method to traverse list and update ServicePermission once found */
    @tailrec
    private def updateOrPrepend(
                                 list: List[ServicePermissions],
                                 targetName: String,
                                 newOps: KeyOperation,
                                 acc: List[ServicePermissions] = Nil): (List[ServicePermissions], Boolean) =
        list match {
            case Nil =>
                ( acc.reverse ::: List(ServicePermissions(serviceName=targetName, allowedOperations=Set(newOps))), true)
            case head :: tail if head.serviceName == targetName =>
                if head.allowedOperations.contains(newOps) then
                    (acc.reverse ::: (head :: tail), false)
                else
                    (acc.reverse ::: (head.copy(allowedOperations = head.allowedOperations.incl(newOps)) :: tail), true)
            case head :: tail =>
                updateOrPrepend(tail, targetName, newOps, head :: acc)
        }

    /** Recursive helper to traverse list and remove ServicePermission, if found */
    @tailrec
    private def remove(
                        list: List[ServicePermissions],
                        targetName: String,
                        opToRemove: KeyOperation,
                        acc: List[ServicePermissions] = Nil): (List[ServicePermissions], Boolean) =
        list match {
            case Nil =>
                (acc.reverse , false)
            case head :: tail if head.serviceName == targetName =>
                if head.allowedOperations.contains(opToRemove) then
                    (acc.reverse ::: (head.copy(allowedOperations = head.allowedOperations - opToRemove) :: tail), true)
                else
                    (acc.reverse ::: (head :: tail), false)
            case head :: tail =>
                remove(tail, targetName, opToRemove, head :: acc)
        }

    extension (p: Permissions)
        def grantOperation(
                            serviceName: String,
                            keyName: String,
                            operation: KeyOperation,
                            rationale: String,
                            changedAt: Instant): Permissions =
            val (updatedPerms, changed) = updateOrPrepend(p.entries, serviceName, operation)
            if changed then
                val changeUpdate = PermissionChange(
                    serviceName=serviceName,
                    keyName=keyName,
                    operation=operation,
                    status=PermissionChangeStatus.Granted,
                    rationale=rationale,
                    changedAt=changedAt
                )

                Permissions(
                    entries=updatedPerms,
                    lastUpdated=changedAt,
                    changeHistory=changeUpdate :: p.changeHistory
                )
            else
                p

        def revokeOperation(
                             serviceName: String,
                             keyName: String,
                             operation: KeyOperation,
                             rationale: String,
                             changedAt: Instant): Permissions =
            val (updatedPerms, changed) = remove(p.entries, serviceName, operation)
            if changed then
                val changeUpdate = PermissionChange(
                    serviceName = serviceName,
                    keyName = keyName,
                    operation = operation,
                    status = PermissionChangeStatus.Revoked,
                    rationale=rationale,
                    changedAt = changedAt
                )

                Permissions(
                    entries = updatedPerms,
                    lastUpdated = changedAt,
                    changeHistory = changeUpdate :: p.changeHistory
                )
            else
                p
