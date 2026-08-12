package com.keystone.kms.domain

/** Representative of a key-level status. This field helps remove ambiguity
  * between what status is tied to a specific version and what status is
  * tied to the key as a whole.
  */
sealed trait KeyStatusNotice

object KeyStatusNotice:
    /** This is the status each key begins. With this notice, key material
      * has not yet been created and the key is currently unusable. Once
      * the key material has been created, the notice is removed.
      */
    case object PendingApproval extends KeyStatusNotice

    /** This status is if the request for a key is denied. With this notice,
      * no key material has been created. This is a terminal state and the key
      * creation request must be resubmitted.
      */
    case object Rejected extends KeyStatusNotice

    /** This status represents a potential compromise of key material. In this
      * state, the flag is set and can only be removed when a new version of key
      * material has been successfully created.
      */
    case object Compromised extends KeyStatusNotice

    /** This status represents a key that has been successfully drained of all
      * dependencies, with no plans of return. This is a terminal state and the
      * existing key is deemed unusable permanently.
      */
    case object Decommissioned extends KeyStatusNotice