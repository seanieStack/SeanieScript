package interp;

import java.util.List;
import lombok.AllArgsConstructor;

abstract class Expr {

  interface Visitor<R> {
    R visitBinaryExpr(Binary expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitUnaryExpr(Unary expr);
  }

  @AllArgsConstructor
  static class Binary extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }
  }

  @AllArgsConstructor
  static class Grouping extends Expr {
    final Expr expression;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
  }

  @AllArgsConstructor
  static class Literal extends Expr {
    final Object value;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
  }

  @AllArgsConstructor
  static class Unary extends Expr {
    final Token operator;
    final Expr right;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
  }


  abstract <R> R accept(Visitor<R> visitor);
}
