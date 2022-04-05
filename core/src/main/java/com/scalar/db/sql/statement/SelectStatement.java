package com.scalar.db.sql.statement;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.scalar.db.sql.ClusteringOrdering;
import com.scalar.db.sql.Predicate;
import com.scalar.db.sql.Projection;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

@Immutable
public class SelectStatement implements DmlStatement {

  public final String namespaceName;
  public final String tableName;
  public final ImmutableList<Projection> projections;
  public final ImmutableList<Predicate> predicates;
  public final ImmutableList<ClusteringOrdering> clusteringOrderings;
  public final int limit;

  private SelectStatement(
      String namespaceName,
      String tableName,
      ImmutableList<Projection> projections,
      ImmutableList<Predicate> predicates,
      ImmutableList<ClusteringOrdering> clusteringOrderings,
      int limit) {
    this.namespaceName = Objects.requireNonNull(namespaceName);
    this.tableName = Objects.requireNonNull(tableName);
    this.projections = Objects.requireNonNull(projections);
    this.predicates = Objects.requireNonNull(predicates);
    this.clusteringOrderings = Objects.requireNonNull(clusteringOrderings);
    this.limit = limit;
  }

  @Override
  public <R, C> R accept(StatementVisitor<R, C> visitor, C context) {
    return visitor.visit(this, context);
  }

  @Override
  public <R, C> R accept(DmlStatementVisitor<R, C> visitor, C context) {
    return visitor.visit(this, context);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("namespaceName", namespaceName)
        .add("tableName", tableName)
        .add("projectedColumnNames", projections)
        .add("predicates", predicates)
        .add("clusteringOrderings", clusteringOrderings)
        .add("limit", limit)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SelectStatement)) {
      return false;
    }
    SelectStatement that = (SelectStatement) o;
    return limit == that.limit
        && Objects.equals(namespaceName, that.namespaceName)
        && Objects.equals(tableName, that.tableName)
        && Objects.equals(projections, that.projections)
        && Objects.equals(predicates, that.predicates)
        && Objects.equals(clusteringOrderings, that.clusteringOrderings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        namespaceName, tableName, projections, predicates, clusteringOrderings, limit);
  }

  public static SelectStatement of(
      String namespaceName,
      String tableName,
      ImmutableList<Projection> projections,
      ImmutableList<Predicate> predicates,
      ImmutableList<ClusteringOrdering> clusteringOrderings,
      int limit) {
    return new SelectStatement(
        namespaceName, tableName, projections, predicates, clusteringOrderings, limit);
  }
}
