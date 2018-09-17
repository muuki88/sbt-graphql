# Change Log

## [v0.10.1](https://github.com/muuki88/sbt-graphql/tree/v0.10.1) (2018-09-17)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.10.0...v0.10.1)

**Closed issues:**

- Implement magic \#import like apollo-graphql [\#48](https://github.com/muuki88/sbt-graphql/issues/48)

**Merged pull requests:**

- Update scalameta to 4.0.0 RC1 [\#53](https://github.com/muuki88/sbt-graphql/pull/53) ([ngbinh](https://github.com/ngbinh))
- Update sangria and cats dependencies [\#52](https://github.com/muuki88/sbt-graphql/pull/52) ([ngbinh](https://github.com/ngbinh))
- Use cats to make some of the document wrangling a bit simpler [\#51](https://github.com/muuki88/sbt-graphql/pull/51) ([felixbr](https://github.com/felixbr))

## [v0.10.0](https://github.com/muuki88/sbt-graphql/tree/v0.10.0) (2018-09-12)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.9.0...v0.10.0)

**Implemented enhancements:**

- FIX \#48 Implement magic \#import like apollo-graphql [\#50](https://github.com/muuki88/sbt-graphql/pull/50) ([muuki88](https://github.com/muuki88))

**Closed issues:**

- Fix generation of interfaces [\#32](https://github.com/muuki88/sbt-graphql/issues/32)

**Merged pull requests:**

- Add imports to interfaces [\#46](https://github.com/muuki88/sbt-graphql/pull/46) ([muuki88](https://github.com/muuki88))

## [v0.9.0](https://github.com/muuki88/sbt-graphql/tree/v0.9.0) (2018-08-10)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.9.1...v0.9.0)

## [v0.9.1](https://github.com/muuki88/sbt-graphql/tree/v0.9.1) (2018-08-10)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.8.2...v0.9.1)

**Implemented enhancements:**

- Add setting for additional imports in generated code [\#29](https://github.com/muuki88/sbt-graphql/issues/29)

**Closed issues:**

- Generate code and decoders for `enum` type [\#44](https://github.com/muuki88/sbt-graphql/issues/44)

**Merged pull requests:**

- FIX \#44 Generate code and decoders for `enum` type [\#45](https://github.com/muuki88/sbt-graphql/pull/45) ([muuki88](https://github.com/muuki88))

## [v0.8.2](https://github.com/muuki88/sbt-graphql/tree/v0.8.2) (2018-07-26)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.8.1...v0.8.2)

## [v0.8.1](https://github.com/muuki88/sbt-graphql/tree/v0.8.1) (2018-07-26)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.8.0...v0.8.1)

**Implemented enhancements:**

- Remove `Document` type from `GraphQLQuery` trait [\#42](https://github.com/muuki88/sbt-graphql/issues/42)

**Merged pull requests:**

- FIX \#42 remove Document` type from `GraphQLQuery` trait [\#43](https://github.com/muuki88/sbt-graphql/pull/43) ([muuki88](https://github.com/muuki88))
- Enable post requests for introspection [\#41](https://github.com/muuki88/sbt-graphql/pull/41) ([muuki88](https://github.com/muuki88))

## [v0.8.0](https://github.com/muuki88/sbt-graphql/tree/v0.8.0) (2018-07-24)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.7.0...v0.8.0)

**Fixed bugs:**

- Add `Data` encoder for `GraphQLQuery` trait [\#38](https://github.com/muuki88/sbt-graphql/issues/38)
- Add `document` to GraphQLQuery trait [\#34](https://github.com/muuki88/sbt-graphql/issues/34)

**Merged pull requests:**

- Fix \#38 data decoder [\#40](https://github.com/muuki88/sbt-graphql/pull/40) ([muuki88](https://github.com/muuki88))
- Fix shadowing warning due to redundant import [\#39](https://github.com/muuki88/sbt-graphql/pull/39) ([felixbr](https://github.com/felixbr))
- Fix \#34 Add document val to GraphQLQuery trait [\#37](https://github.com/muuki88/sbt-graphql/pull/37) ([muuki88](https://github.com/muuki88))
- Added user defined imports to codegen \#29 / \#33 [\#36](https://github.com/muuki88/sbt-graphql/pull/36) ([muuki88](https://github.com/muuki88))

## [v0.7.0](https://github.com/muuki88/sbt-graphql/tree/v0.7.0) (2018-07-23)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.6.0...v0.7.0)

**Implemented enhancements:**

- Support Input-Object-Values in Apollo Style [\#25](https://github.com/muuki88/sbt-graphql/issues/25)

**Closed issues:**

- Upgrade to scalameta 3.x [\#27](https://github.com/muuki88/sbt-graphql/issues/27)
- codegen not generating custom scalar types. [\#26](https://github.com/muuki88/sbt-graphql/issues/26)

**Merged pull requests:**

- Improve README [\#31](https://github.com/muuki88/sbt-graphql/pull/31) ([jonas](https://github.com/jonas))
- Upgrade to Scalameta 3.7.4 [\#30](https://github.com/muuki88/sbt-graphql/pull/30) ([muuki88](https://github.com/muuki88))
- FIX \#25 Add more documentation and a test for nested input types [\#28](https://github.com/muuki88/sbt-graphql/pull/28) ([muuki88](https://github.com/muuki88))

## [v0.6.0](https://github.com/muuki88/sbt-graphql/tree/v0.6.0) (2018-05-31)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.5.0...v0.6.0)

**Closed issues:**

- Trouble getting codegen to work [\#21](https://github.com/muuki88/sbt-graphql/issues/21)
- Remove the IntegrationTest scope from the GraphQLQueryPlugin [\#18](https://github.com/muuki88/sbt-graphql/issues/18)

**Merged pull requests:**

- Initial draft for json codec code generation [\#23](https://github.com/muuki88/sbt-graphql/pull/23) ([muuki88](https://github.com/muuki88))
- FIX \#18 Remove the IntegrationTest scope from the GraphQLQueryPlugin [\#20](https://github.com/muuki88/sbt-graphql/pull/20) ([muuki88](https://github.com/muuki88))

## [v0.5.0](https://github.com/muuki88/sbt-graphql/tree/v0.5.0) (2018-03-17)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.4.1...v0.5.0)

**Implemented enhancements:**

- Generate code from queries [\#4](https://github.com/muuki88/sbt-graphql/issues/4)

**Merged pull requests:**

- Fix \#4: Add code generator [\#15](https://github.com/muuki88/sbt-graphql/pull/15) ([jonas](https://github.com/jonas))
- Deploy snapshot branch [\#14](https://github.com/muuki88/sbt-graphql/pull/14) ([jonas](https://github.com/jonas))
- Extract the release notes generator [\#13](https://github.com/muuki88/sbt-graphql/pull/13) ([muuki88](https://github.com/muuki88))

## [v0.4.1](https://github.com/muuki88/sbt-graphql/tree/v0.4.1) (2017-12-01)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.4.0...v0.4.1)

**Merged pull requests:**

- Deploy tags as well [\#12](https://github.com/muuki88/sbt-graphql/pull/12) ([muuki88](https://github.com/muuki88))
- Move publishing build steps to the deploy phase [\#10](https://github.com/muuki88/sbt-graphql/pull/10) ([jonas](https://github.com/jonas))
- Update sbt, Scala and Sangria versions [\#9](https://github.com/muuki88/sbt-graphql/pull/9) ([jonas](https://github.com/jonas))

## [v0.4.0](https://github.com/muuki88/sbt-graphql/tree/v0.4.0) (2017-10-30)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.3.0...v0.4.0)

**Implemented enhancements:**

- Scripted tests [\#3](https://github.com/muuki88/sbt-graphql/issues/3)

**Merged pull requests:**

- Fix graphql typos [\#7](https://github.com/muuki88/sbt-graphql/pull/7) ([jonas](https://github.com/jonas))
- Add scripted tests [\#6](https://github.com/muuki88/sbt-graphql/pull/6) ([muuki88](https://github.com/muuki88))
- Use the introspection query provided by Sangria [\#2](https://github.com/muuki88/sbt-graphql/pull/2) ([jonas](https://github.com/jonas))
- Minor fixes [\#1](https://github.com/muuki88/sbt-graphql/pull/1) ([jonas](https://github.com/jonas))

## [v0.3.0](https://github.com/muuki88/sbt-graphql/tree/v0.3.0) (2017-10-25)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.2.0...v0.3.0)

## [v0.2.0](https://github.com/muuki88/sbt-graphql/tree/v0.2.0) (2017-10-22)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.1.4...v0.2.0)

## [v0.1.4](https://github.com/muuki88/sbt-graphql/tree/v0.1.4) (2017-09-21)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.1.3...v0.1.4)

## [v0.1.3](https://github.com/muuki88/sbt-graphql/tree/v0.1.3) (2017-09-21)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.1.2...v0.1.3)

## [v0.1.2](https://github.com/muuki88/sbt-graphql/tree/v0.1.2) (2017-09-21)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.1.1...v0.1.2)

## [v0.1.1](https://github.com/muuki88/sbt-graphql/tree/v0.1.1) (2017-09-19)
[Full Changelog](https://github.com/muuki88/sbt-graphql/compare/v0.1.0...v0.1.1)

## [v0.1.0](https://github.com/muuki88/sbt-graphql/tree/v0.1.0) (2017-09-17)


\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*