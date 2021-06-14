// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: scalardb.proto

package com.scalar.db.rpc;

public interface TransactionalMutateRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:rpc.TransactionalMutateRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string transaction_id = 1;</code>
   * @return The transactionId.
   */
  java.lang.String getTransactionId();
  /**
   * <code>string transaction_id = 1;</code>
   * @return The bytes for transactionId.
   */
  com.google.protobuf.ByteString
      getTransactionIdBytes();

  /**
   * <code>repeated .rpc.Mutation mutations = 2;</code>
   */
  java.util.List<com.scalar.db.rpc.Mutation> 
      getMutationsList();
  /**
   * <code>repeated .rpc.Mutation mutations = 2;</code>
   */
  com.scalar.db.rpc.Mutation getMutations(int index);
  /**
   * <code>repeated .rpc.Mutation mutations = 2;</code>
   */
  int getMutationsCount();
  /**
   * <code>repeated .rpc.Mutation mutations = 2;</code>
   */
  java.util.List<? extends com.scalar.db.rpc.MutationOrBuilder> 
      getMutationsOrBuilderList();
  /**
   * <code>repeated .rpc.Mutation mutations = 2;</code>
   */
  com.scalar.db.rpc.MutationOrBuilder getMutationsOrBuilder(
      int index);
}
